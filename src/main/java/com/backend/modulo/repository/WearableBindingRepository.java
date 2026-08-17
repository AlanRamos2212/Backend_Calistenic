package com.backend.modulo.repository;

import com.backend.modulo.entity.WearableBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WearableBindingRepository extends JpaRepository<WearableBinding, Long> {
    List<WearableBinding> findByUser_Id(Long userId);

    List<WearableBinding> findByUser_IdAndActiveTrue(Long userId);

    List<WearableBinding> findByWearableId(String wearableId);

    Optional<WearableBinding> findByWearableIdAndActiveTrue(String wearableId);

    Optional<WearableBinding> findByDeviceToken(String deviceToken);
}
