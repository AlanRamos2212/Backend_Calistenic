package com.backend.modulo.repository;

import com.backend.modulo.entity.WearableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface WearableSessionRepository extends JpaRepository<WearableSession, Long> {
    List<WearableSession> findByUser_IdOrderByStartedAtDesc(Long userId);

    List<WearableSession> findByUser_IdAndStatusOrderByStartedAtDesc(Long userId, WearableSession.Status status);

    List<WearableSession> findByExercise_IdOrderByStartedAtDesc(Long exerciseId);

    List<WearableSession> findByUser_IdAndStartedAtBetweenOrderByStartedAtAsc(
            Long userId, OffsetDateTime from, OffsetDateTime to);
}
