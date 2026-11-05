package com.backend.modulo.repository;

import com.backend.modulo.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);
    long countByPost_Id(Long postId);
    void deleteByPost_IdAndUser_Id(Long postId, Long userId);
}
