package com.backend.modulo.repository;

import com.backend.modulo.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUser_IdOrderByUnlockedAtDesc(Long userId);
    Optional<Achievement> findByIdAndUser_Id(Long id, Long userId);
    Optional<Achievement> findByUser_IdAndCode(Long userId, String code);
}
