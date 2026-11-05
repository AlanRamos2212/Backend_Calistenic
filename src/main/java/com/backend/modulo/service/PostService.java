package com.backend.modulo.service;

import com.backend.modulo.dto.*;
import com.backend.modulo.entity.*;
import com.backend.modulo.document.PostDocument;
import com.backend.modulo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final PostCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final WearableSessionRepository sessionRepository;
    private final MediaStorage mediaStorageService;

    /** Content types accepted for optional post images. */
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5 MB

    /**
     * Optional: Elasticsearch may not be running in local dev.
     * When absent, indexing/search via ES is silently skipped.
     */
    private final Optional<PostSearchRepository> postSearchRepository;

    @Autowired
    public PostService(PostRepository postRepository,
                       PostLikeRepository likeRepository,
                       PostCommentRepository commentRepository,
                       UserRepository userRepository,
                       WearableSessionRepository sessionRepository,
                       com.backend.modulo.service.MediaStorage mediaStorageService,
                       Optional<PostSearchRepository> postSearchRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.mediaStorageService = mediaStorageService;
        this.postSearchRepository = postSearchRepository;
    }

    @Transactional
    public PostResponse create(Long userId, CreatePostRequest request) {
        return create(userId, request, null);
    }

    /** Creates a post with an optional image; a null/empty image yields a plain text post. */
    @Transactional
    public PostResponse create(Long userId, CreatePostRequest request, MultipartFile image) {
        User user = user(userId);
        Post.PostBuilder builder = Post.builder().user(user).content(request.getContent().trim());
        if (request.getWorkoutSessionId() != null) {
            WearableSession session = sessionRepository.findById(request.getWorkoutSessionId())
                    .orElseThrow(() -> notFound("Sesión no encontrada."));
            if (!session.getUser().getId().equals(userId)) throw forbidden();
            builder.workoutSession(session);
        }
        if (image != null && !image.isEmpty()) {
            validateImage(image);
            try {
                builder.imageUrl(mediaStorageService.store(image));
            } catch (java.io.IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen.");
            }
        }
        Post saved = postRepository.save(builder.build());
        index(saved);
        return response(saved, userId);
    }

    private void validateImage(MultipartFile image) {
        if (image.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(image.getContentType().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado (usa JPG, PNG, WEBP o GIF).");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La imagen no puede exceder 5 MB.");
        }
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> feed(Long userId, Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(post -> response(post, userId));
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id, Long userId) {
        return response(post(id), userId);
    }

    @Transactional
    public PostResponse update(Long id, Long userId, CreatePostRequest request) {
        Post post = ownedPost(id, userId);
        post.setContent(request.getContent().trim());
        Post saved = postRepository.save(post);
        index(saved);
        return response(saved, userId);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Post post = ownedPost(id, userId);
        String imageUrl = post.getImageUrl();
        postRepository.delete(post);
        postSearchRepository.ifPresent(repo -> repo.deleteById(String.valueOf(id)));
        deleteLocalMedia(imageUrl);
    }

    private void deleteLocalMedia(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            mediaStorageService.delete(imageUrl);
        } catch (java.io.IOException ex) {
            // The post is already deleted; keep the API successful and retain
            // a diagnostic so the orphan can be cleaned up later.
            log.warn("No se pudo eliminar el archivo multimedia del post: {}", imageUrl, ex);
        }
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        post(postId);
        if (likeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            likeRepository.deleteByPost_IdAndUser_Id(postId, userId);
            return false;
        }
        likeRepository.save(PostLike.builder().post(post(postId)).user(user(userId)).build());
        return true;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> comments(Long postId) {
        post(postId);
        return commentRepository.findByPost_IdOrderByCreatedAtAsc(postId).stream().map(this::commentResponse).toList();
    }

    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CommentRequest request) {
        PostComment comment = PostComment.builder().post(post(postId)).user(user(userId))
                .content(request.getContent().trim()).build();
        return commentResponse(commentRepository.save(comment));
    }

    private PostResponse response(Post post, Long userId) {
        return PostResponse.builder().id(post.getId()).userId(post.getUser().getId())
                .userName(post.getUser().getName())
                .userAvatar(post.getUser().getAvatarUrl())
                .content(post.getContent()).imageUrl(post.getImageUrl())
                .workoutSessionId(post.getWorkoutSession() == null ? null : post.getWorkoutSession().getId())
                .likesCount(likeRepository.countByPost_Id(post.getId()))
                .commentsCount(commentRepository.countByPost_Id(post.getId()))
                .likedByCurrentUser(likeRepository.existsByPost_IdAndUser_Id(post.getId(), userId))
                .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).build();
    }

    private CommentResponse commentResponse(PostComment comment) {
        return CommentResponse.builder().id(comment.getId()).postId(comment.getPost().getId())
                .userId(comment.getUser().getId()).userName(comment.getUser().getName())
                .userAvatar(comment.getUser().getAvatarUrl())
                .content(comment.getContent()).createdAt(comment.getCreatedAt()).build();
    }

    private Post ownedPost(Long id, Long userId) {
        Post post = post(id);
        if (!post.getUser().getId().equals(userId)) throw forbidden();
        return post;
    }

    private Post post(Long id) { return postRepository.findById(id).orElseThrow(() -> notFound("Post no encontrado.")); }
    private User user(Long id) { return userRepository.findById(id).orElseThrow(() -> notFound("Usuario no encontrado.")); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    private ResponseStatusException forbidden() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos sobre este recurso."); }

    private void index(Post post) {
        postSearchRepository.ifPresent(repo -> repo.save(
            PostDocument.builder()
                .id(String.valueOf(post.getId()))
                .content(post.getContent())
                .userId(post.getUser().getId())
                .userName(post.getUser().getName())
                .createdAt(post.getCreatedAt())
                .build()
        ));
    }
}
