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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final PostCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final WearableSessionRepository sessionRepository;

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
                       Optional<PostSearchRepository> postSearchRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.postSearchRepository = postSearchRepository;
    }

    @Transactional
    public PostResponse create(Long userId, CreatePostRequest request) {
        User user = user(userId);
        Post.PostBuilder builder = Post.builder().user(user).content(request.getContent().trim());
        if (request.getWorkoutSessionId() != null) {
            WearableSession session = sessionRepository.findById(request.getWorkoutSessionId())
                    .orElseThrow(() -> notFound("Sesión no encontrada."));
            if (!session.getUser().getId().equals(userId)) throw forbidden();
            builder.workoutSession(session);
        }
        Post saved = postRepository.save(builder.build());
        index(saved);
        return response(saved, userId);
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
        postRepository.delete(post);
        postSearchRepository.ifPresent(repo -> repo.deleteById(String.valueOf(id)));
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
                .userName(post.getUser().getName()).content(post.getContent())
                .workoutSessionId(post.getWorkoutSession() == null ? null : post.getWorkoutSession().getId())
                .likesCount(likeRepository.countByPost_Id(post.getId()))
                .commentsCount(commentRepository.countByPost_Id(post.getId()))
                .likedByCurrentUser(likeRepository.existsByPost_IdAndUser_Id(post.getId(), userId))
                .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).build();
    }

    private CommentResponse commentResponse(PostComment comment) {
        return CommentResponse.builder().id(comment.getId()).postId(comment.getPost().getId())
                .userId(comment.getUser().getId()).userName(comment.getUser().getName())
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
