package com.backend.modulo.controller;

import com.backend.modulo.dto.DeviceTokenResponse;
import com.backend.modulo.dto.WearableBindingResponse;
import com.backend.modulo.entity.User;
import com.backend.modulo.entity.WearableBinding;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.security.JwtTokenProvider;
import com.backend.modulo.service.WearableBindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wearables")
@RequiredArgsConstructor
public class WearableController {

    private final WearableBindingService wearableBindingService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/bind")
    public ResponseEntity<DeviceTokenResponse> bindWearable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody java.util.Map<String, String> body) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        String wearableId = body.get("wearableId");
        if (wearableId == null || wearableId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        String deviceToken = jwtTokenProvider.generateDeviceToken(userDetails);

        wearableBindingService.bind(user, wearableId, deviceToken);

        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie
                .from("app.device.token", deviceToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(900)
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .body(DeviceTokenResponse.builder()
                        .deviceToken(deviceToken)
                        .userId(String.valueOf(user.getId()))
                        .tokenType("Bearer")
                        .expiresInMs(900000L)
                        .build());
    }

    @GetMapping
    public ResponseEntity<List<WearableBindingResponse>> listBindings(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        List<WearableBindingResponse> bindings = wearableBindingService.findByUserId(user.getId()).stream()
                .map(WearableBindingResponse::from)
                .toList();
        return ResponseEntity.ok(bindings);
    }

    @PostMapping("/{wearableId}/confirm-pin")
    public ResponseEntity<?> confirmPin(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String wearableId) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        try {
            wearableBindingService.confirmPinForUser(wearableId, user.getId());
            return ResponseEntity.ok().build();
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{wearableId}")
    public ResponseEntity<?> unbindWearable(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String wearableId) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        wearableBindingService.unbindWearable(wearableId, user.getId());
        return ResponseEntity.ok().build();
    }
}
