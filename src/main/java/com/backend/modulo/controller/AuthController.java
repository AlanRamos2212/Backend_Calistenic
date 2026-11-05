package com.backend.modulo.controller;

import com.backend.modulo.dto.AvatarUploadResponse;
import com.backend.modulo.dto.AuthResponse;
import com.backend.modulo.dto.DeviceTokenResponse;
import com.backend.modulo.dto.LoginRequest;
import com.backend.modulo.dto.RegisterRequest;
import com.backend.modulo.dto.UpdateProfileRequest;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.security.JwtTokenProvider;
import com.backend.modulo.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final UserDetailsService userDetailsService;
        private final MediaStorageService mediaStorageService;

        @PostMapping("/register")
        public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(java.util.Map.of("message", "Ese correo ya está registrado."));
                }

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(User.Role.USER)
                                .build();

                userRepository.save(user);

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String token = jwtTokenProvider.generateToken(userDetails);

                return ResponseEntity.ok(AuthResponse.builder()
                                .token(token)
                                .email(user.getEmail())
                                .name(user.getName())
                                .role(user.getRole().name())
                                .build());
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña inválidos."));

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String token = jwtTokenProvider.generateToken(userDetails);

                return ResponseEntity.ok(AuthResponse.builder()
                                .token(token)
                                .email(user.getEmail())
                                .name(user.getName())
                                .role(user.getRole().name())
                                .build());
        }

        @GetMapping("/me")
        public ResponseEntity<?> getMyProfile(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails) {
                if (userDetails == null) {
                        return ResponseEntity.status(401).body("Error: Usuario no autenticado.");
                }

                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado en la base de datos."));

                return ResponseEntity.ok(toProfileResponse(user));
        }

        /**
         * Update the authenticated user's display name.
         * <p>
         * {@code PATCH /api/auth/me} — body: {@code {"name": "Nuevo Nombre"}}
         */
        @PatchMapping("/me")
        public ResponseEntity<?> updateMyProfile(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody UpdateProfileRequest request) {
                if (userDetails == null) {
                        return ResponseEntity.status(401).body("Error: Usuario no autenticado.");
                }

                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado en la base de datos."));

                user.setName(request.getName().trim());
                userRepository.save(user);

                return ResponseEntity.ok(toProfileResponse(user));
        }

        /**
         * Upload a new avatar photo for the authenticated user.
         * <p>
         * {@code POST /api/auth/avatar} — multipart/form-data field: {@code avatar}
         * Returns {@code {"filename": "...", "avatarUrl": "..."}} on success.
         */
        @PostMapping(value = "/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadAvatar(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam("avatar") MultipartFile file) {
                if (userDetails == null) {
                        return ResponseEntity.status(401).body("Error: Usuario no autenticado.");
                }
                if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("message", "El archivo de imagen está vacío."));
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("message", "Solo se permiten archivos de imagen."));
                }

                try {
                        String filename = mediaStorageService.store(file);
                        // Persist avatarUrl on the user record so /me returns it
                        User user = userRepository.findByEmail(userDetails.getUsername())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Usuario no encontrado en la base de datos."));
                        user.setAvatarUrl(filename);
                        userRepository.save(user);

                        return ResponseEntity.ok(AvatarUploadResponse.builder()
                                        .filename(filename)
                                        .avatarUrl("/api/media/" + filename)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(java.util.Map.of("message", "No se pudo guardar la imagen: " + e.getMessage()));
                }
        }

        @PostMapping("/device-token")
        public ResponseEntity<?> exchangeDeviceToken(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails) {
                if (userDetails == null) {
                        return ResponseEntity.status(401).body("Error: Usuario no autenticado.");
                }

                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado en la base de datos."));

                String deviceToken = jwtTokenProvider.generateDeviceToken(userDetails);

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

        // ── helpers ──────────────────────────────────────────────────────────────

        private com.backend.modulo.dto.UserProfileResponse toProfileResponse(User user) {
                return com.backend.modulo.dto.UserProfileResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .createdAt(user.getCreatedAt())
                                .avatarUrl(user.getAvatarUrl())
                                .build();
        }
}
