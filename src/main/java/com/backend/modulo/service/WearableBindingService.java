package com.backend.modulo.service;

import com.backend.modulo.entity.User;
import com.backend.modulo.entity.WearableBinding;
import com.backend.modulo.repository.WearableBindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WearableBindingService {

    private final WearableBindingRepository wearableBindingRepository;

    public WearableBinding bind(User user, String wearableId, String deviceToken) {
        wearableBindingRepository.findByUser_IdAndActiveTrue(user.getId())
                .forEach(this::deactivateBinding);

        wearableBindingRepository.findByWearableIdAndActiveTrue(wearableId)
                .ifPresent(this::deactivateBinding);

        WearableBinding b = WearableBinding.builder()
                .user(user)
                .wearableId(wearableId)
                .deviceToken(deviceToken)
                .pairedAt(OffsetDateTime.now())
                .active(true)
                .build();
        return wearableBindingRepository.save(b);
    }

    public List<WearableBinding> findByUserId(Long userId) {
        return wearableBindingRepository.findByUser_Id(userId);
    }

    public void deactivateBinding(WearableBinding binding) {
        binding.setActive(false);
        wearableBindingRepository.save(binding);
    }

    public WearableBinding confirmPin(String wearableId) {
        var opt = wearableBindingRepository.findByWearableIdAndActiveTrue(wearableId);
        if (opt.isEmpty())
            throw new IllegalArgumentException("Binding not found");
        WearableBinding b = opt.get();
        b.setPinConfirmed(true);
        b.setConfirmedAt(OffsetDateTime.now());
        return wearableBindingRepository.save(b);
    }

    public void unbindWearable(String wearableId, Long userId) {
        var opt = wearableBindingRepository.findByWearableIdAndActiveTrue(wearableId);
        opt.ifPresent(binding -> {
            if (binding.getUser().getId().equals(userId)) {
                this.deactivateBinding(binding);
            }
        });
    }

}
