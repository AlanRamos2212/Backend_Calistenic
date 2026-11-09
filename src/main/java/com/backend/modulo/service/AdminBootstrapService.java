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
 * Es idempotente: crea el admin si no existe, y actualiza rol/contraseña
 * si el email ya pertenece a otro rol.
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
        User admin = userRepository.findByEmail(ADMIN_EMAIL).map(u -> {
            boolean changed = false;
            if (u.getRole() != User.Role.ADMIN) {
                u.setRole(User.Role.ADMIN);
                changed = true;
            }
            if (passwordEncoder.matches(ADMIN_PASSWORD, u.getPassword()) == false) {
                u.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                changed = true;
            }
            if (changed) {
                return userRepository.save(u);
            }
            return u;
        }).orElseGet(() -> {
            User nuevo = User.builder()
                    .name(ADMIN_NAME)
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(User.Role.ADMIN)
                    .build();
            return userRepository.save(nuevo);
        });
        log.info("Admin de soporte listo: {} (id={}, role={})", admin.getEmail(), admin.getId(), admin.getRole());
    }
}
