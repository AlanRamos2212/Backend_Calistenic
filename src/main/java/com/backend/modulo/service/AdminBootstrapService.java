package com.backend.modulo.service;

import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Admin bootstrap (Practica 7 - Soporte tecnico): garantiza que exista un
 * usuario ADMIN en la base de datos para el flujo de reportes de soporte.
 * Es idempotente: solo crea el admin si el email no esta registrado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapService implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "qa-admin-media@example.com";
    private static final String ADMIN_PASSWORD = "QaAdmin2026!";
    private static final String ADMIN_NAME = "QA Admin Media";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.debug("Admin '{}' ya existe en la base de datos. Bootstrap omitido.", ADMIN_EMAIL);
            return;
        }
        User admin = User.builder()
                .name(ADMIN_NAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(User.Role.ADMIN)
                .build();
        User saved = userRepository.save(admin);
        log.info("Admin de soporte creado: {} (id={})", saved.getEmail(), saved.getId());
    }
}
