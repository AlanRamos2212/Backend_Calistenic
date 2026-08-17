package com.backend.modulo.controller;

import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.service.WearableSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiCompatibilityController {

    private final WearableSessionService wearableSessionService;
    private final UserRepository userRepository;

    @GetMapping("/api/statistics")
    public ResponseEntity<?> statistics(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        Long resolvedUserId = resolveCurrentUserId(principal, userId);
        return ResponseEntity.ok(wearableSessionService.getStatsForUser(resolvedUserId));
    }

    private Long resolveCurrentUserId(UserDetails principal, Long fallbackUserId) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
            return user.getId();
        }
        if (fallbackUserId != null) {
            return fallbackUserId;
        }
        throw new IllegalArgumentException("Usuario no autenticado.");
    }
}
