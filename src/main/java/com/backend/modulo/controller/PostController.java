package com.backend.modulo.controller;

import com.backend.modulo.dto.*;
import com.backend.modulo.entity.User;
import com.backend.modulo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final com.backend.modulo.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> feed(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(postService.feed(user(principal).getId(), PageRequest.of(Math.max(page, 0), Math.min(size, 50))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(postService.get(id, user(principal).getId()));
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request,
                                               @AuthenticationPrincipal UserDetails principal) {
        PostResponse response = postService.create(user(principal).getId(), request);
        return ResponseEntity.created(URI.create("/api/posts/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable Long id, @Valid @RequestBody CreatePostRequest request,
                                               @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(postService.update(id, user(principal).getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        postService.delete(id, user(principal).getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        boolean liked = postService.toggleLike(id, user(principal).getId());
        return ResponseEntity.ok(java.util.Map.of("postId", id, "liked", liked));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> comments(@PathVariable Long id) {
        return ResponseEntity.ok(postService.comments(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> comment(@PathVariable Long id, @Valid @RequestBody CommentRequest request,
                                                   @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(postService.addComment(id, user(principal).getId(), request));
    }

    private User user(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
