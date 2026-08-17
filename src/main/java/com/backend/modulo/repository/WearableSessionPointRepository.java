package com.backend.modulo.repository;

import com.backend.modulo.entity.WearableSessionPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WearableSessionPointRepository extends JpaRepository<WearableSessionPoint, Long> {
    List<WearableSessionPoint> findBySession_IdOrderByTimestampAsc(Long sessionId);
}
