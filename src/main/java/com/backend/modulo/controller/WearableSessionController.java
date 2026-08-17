package com.backend.modulo.controller;

import com.backend.modulo.dto.WearableSessionMetricsRequest;
import com.backend.modulo.dto.WearableSessionResponse;
import com.backend.modulo.dto.WearableSessionStartRequest;
import com.backend.modulo.dto.WearableSessionStopRequest;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.service.WearableSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wearable-sessions")
@RequiredArgsConstructor
public class WearableSessionController {

    private final WearableSessionService wearableSessionService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    public ResponseEntity<WearableSessionResponse> start(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody WearableSessionStartRequest request) {
        request.setUserId(resolveCurrentUserId(principal, request.getUserId()));
        return ResponseEntity.ok(wearableSessionService.startSession(request));
    }

    @PostMapping("/{sessionId}/metrics")
    public ResponseEntity<WearableSessionResponse> metrics(
            @PathVariable Long sessionId,
            @RequestBody WearableSessionMetricsRequest request) {
        return ResponseEntity.ok(wearableSessionService.addMetrics(sessionId, request));
    }

    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<WearableSessionResponse> stop(
            @PathVariable Long sessionId,
            @RequestBody WearableSessionStopRequest request) {
        return ResponseEntity.ok(wearableSessionService.stopSession(sessionId, request));
    }

    @GetMapping
    public ResponseEntity<List<WearableSessionResponse>> list(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        Long resolvedUserId = resolveCurrentUserId(principal, userId);
        return ResponseEntity.ok(wearableSessionService.getSessionsForUser(resolvedUserId));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<WearableSessionResponse>> latest(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails principal) {
        Long resolvedUserId = resolveCurrentUserId(principal, userId);
        return ResponseEntity.ok(wearableSessionService.getLatestSessions(resolvedUserId, limit));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<WearableSessionResponse> getById(@PathVariable Long sessionId) {
        return ResponseEntity.ok(wearableSessionService.getSession(sessionId));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        Long resolvedUserId = resolveCurrentUserId(principal, userId);
        return ResponseEntity.ok(wearableSessionService.getStatsForUser(resolvedUserId));
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<WearableSessionResponse>> byExercise(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(wearableSessionService.getExerciseSessions(exerciseId));
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
