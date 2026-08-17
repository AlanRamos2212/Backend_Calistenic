package com.backend.modulo.controller;

import com.backend.modulo.dto.AuthResponse;
import com.backend.modulo.dto.DeviceTokenResponse;
import com.backend.modulo.dto.LoginRequest;
import com.backend.modulo.dto.RegisterRequest;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final UserDetailsService userDetailsService;

        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        return ResponseEntity.badRequest().body("Error: El email ya está registrado.");
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
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
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

                return ResponseEntity.ok(com.backend.modulo.dto.UserProfileResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .createdAt(user.getCreatedAt())
                                .build());
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

                // Create a cookie with the device token. In development we set SameSite=None to
                // allow
                // the cookie to be sent from iframe/XHR; in production set Secure=true.
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
}
