package com.backend.modulo.repository;

import com.backend.modulo.entity.WearablePairingCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WearablePairingCodeRepository extends JpaRepository<WearablePairingCode, Long> {
    List<WearablePairingCode> findByUser_IdAndActiveTrue(Long userId);

    Optional<WearablePairingCode> findFirstByUser_IdAndActiveTrueAndConsumedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<WearablePairingCode> findFirstByCodeAndActiveTrueAndConsumedAtIsNullOrderByCreatedAtDesc(String code);

    Optional<WearablePairingCode> findByCode(String code);
}
