package com.backend.modulo.repository;

import com.backend.modulo.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPost_IdOrderByCreatedAtAsc(Long postId);
    long countByPost_Id(Long postId);
}
