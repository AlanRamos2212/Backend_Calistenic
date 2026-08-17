package com.backend.modulo.service;

import com.backend.modulo.dto.DeviceTokenResponse;
import com.backend.modulo.dto.WearablePairingCodeResponse;
import com.backend.modulo.dto.WearablePairingStatusResponse;
import com.backend.modulo.entity.User;
import com.backend.modulo.entity.WearableBinding;
import com.backend.modulo.entity.WearablePairingCode;
import com.backend.modulo.repository.WearablePairingCodeRepository;
import com.backend.modulo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WearablePairingCodeService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRES_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WearablePairingCodeRepository pairingCodeRepository;
    private final WearableBindingService wearableBindingService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public WearablePairingCodeResponse requestCode(User user) {
        var active = pairingCodeRepository
                .findFirstByUser_IdAndActiveTrueAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId());
        if (active.isPresent() && active.get().isUsable()) {
            return toResponse(active.get());
        }

        deactivatePendingCodes(user.getId());

        WearablePairingCode pairingCode = WearablePairingCode.builder()
                .code(generateUniqueCode())
                .user(user)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusMinutes(EXPIRES_MINUTES))
                .active(true)
                .build();

        pairingCode = pairingCodeRepository.save(pairingCode);
        return toResponse(pairingCode);
    }

    @Transactional
    public DeviceTokenResponse confirmCode(String code, String wearableId) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            throw new IllegalArgumentException("El código debe tener 6 dígitos.");
        }
        if (wearableId == null || wearableId.isBlank()) {
            throw new IllegalArgumentException("El wearableId es obligatorio.");
        }

        WearablePairingCode pairingCode = pairingCodeRepository
                .findFirstByCodeAndActiveTrueAndConsumedAtIsNullOrderByCreatedAtDesc(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado."));

        if (pairingCode.isExpired()) {
            pairingCode.setActive(false);
            pairingCodeRepository.save(pairingCode);
            throw new IllegalArgumentException("Código expirado. Solicita uno nuevo.");
        }

        User user = pairingCode.getUser();
        String deviceToken = jwtTokenProvider.generateDeviceToken(user);
        wearableBindingService.bind(user, wearableId.trim(), deviceToken);

        pairingCode.setConsumedAt(OffsetDateTime.now());
        pairingCode.setActive(false);
        pairingCodeRepository.save(pairingCode);

        return DeviceTokenResponse.builder()
                .deviceToken(deviceToken)
                .userId(String.valueOf(user.getId()))
                .tokenType("Bearer")
                .expiresInMs(900_000L)
                .build();
    }

    public WearablePairingStatusResponse getStatusByCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            return WearablePairingStatusResponse.builder()
                    .code(code)
                    .status("INVALID")
                    .build();
        }

        WearablePairingCode pairingCode = pairingCodeRepository.findByCode(normalizedCode)
                .orElse(null);
        if (pairingCode == null) {
            return WearablePairingStatusResponse.builder()
                    .code(normalizedCode)
                    .status("INVALID")
                    .build();
        }

        if (pairingCode.isExpired()) {
            return WearablePairingStatusResponse.builder()
                    .code(normalizedCode)
                    .status("EXPIRED")
                    .expiresAt(pairingCode.getExpiresAt())
                    .build();
        }

        var bindings = wearableBindingService.findByUserId(pairingCode.getUser().getId());
        WearableBinding activeBinding = bindings.stream()
                .filter(binding -> Boolean.TRUE.equals(binding.getActive()))
                .findFirst()
                .orElse(null);

        if (activeBinding != null && Boolean.TRUE.equals(activeBinding.getPinConfirmed())) {
            return WearablePairingStatusResponse.builder()
                    .code(normalizedCode)
                    .status("CONFIRMED")
                    .wearableId(activeBinding.getWearableId())
                    .pinConfirmed(true)
                    .expiresAt(pairingCode.getExpiresAt())
                    .build();
        }

        if (activeBinding != null) {
            return WearablePairingStatusResponse.builder()
                    .code(normalizedCode)
                    .status("BOUND_WAITING_PIN")
                    .wearableId(activeBinding.getWearableId())
                    .pinConfirmed(false)
                    .expiresAt(pairingCode.getExpiresAt())
                    .build();
        }

        return WearablePairingStatusResponse.builder()
                .code(normalizedCode)
                .status("PENDING")
                .expiresAt(pairingCode.getExpiresAt())
                .build();
    }

    private WearablePairingCodeResponse toResponse(WearablePairingCode code) {
        long expiresInSeconds = ChronoUnit.SECONDS.between(OffsetDateTime.now(), code.getExpiresAt());
        return WearablePairingCodeResponse.builder()
                .code(code.getCode())
                .expiresAt(code.getExpiresAt())
                .expiresInSeconds(Math.max(0, expiresInSeconds))
                .build();
    }

    private void deactivatePendingCodes(Long userId) {
        List<WearablePairingCode> pendingCodes = pairingCodeRepository.findByUser_IdAndActiveTrue(userId);
        for (WearablePairingCode pendingCode : pendingCodes) {
            if (pendingCode.getConsumedAt() == null) {
                pendingCode.setActive(false);
                pairingCodeRepository.save(pendingCode);
            }
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            int maxValue = (int) Math.pow(10, CODE_LENGTH);
            String candidate = String.format("%0" + CODE_LENGTH + "d", RANDOM.nextInt(maxValue));
            if (pairingCodeRepository.findByCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de vinculación único.");
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        return trimmed.matches("\\d{6}") ? trimmed : null;
    }
}
