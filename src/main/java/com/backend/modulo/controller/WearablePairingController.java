package com.backend.modulo.controller;

import com.backend.modulo.dto.DeviceTokenResponse;
import com.backend.modulo.dto.WearablePairingCodeResponse;
import com.backend.modulo.dto.WearablePairingConfirmRequest;
import com.backend.modulo.dto.WearablePairingStatusResponse;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.service.WearablePairingCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wearables/pairing")
@RequiredArgsConstructor
public class WearablePairingController {

    private final WearablePairingCodeService pairingCodeService;
    private final UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<WearablePairingCodeResponse> requestCode(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

            return ResponseEntity.ok(pairingCodeService.requestCode(user));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<DeviceTokenResponse> confirmCode(@RequestBody WearablePairingConfirmRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().build();
            }

            DeviceTokenResponse response = pairingCodeService.confirmCode(request.getCode(), request.getWearableId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{code}")
    public ResponseEntity<WearablePairingStatusResponse> getStatus(@PathVariable String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(pairingCodeService.getStatusByCode(code));
    }
}
