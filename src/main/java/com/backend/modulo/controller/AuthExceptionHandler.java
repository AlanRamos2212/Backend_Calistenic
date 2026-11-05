package com.backend.modulo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

/** Keeps authentication failures from revealing whether an account exists. */
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    private static final String GENERIC_MESSAGE = "No se pudo completar la autenticación.";

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationFailure(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", GENERIC_MESSAGE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        // Avoid echoing submitted values while giving registration users the accepted constraints.
        String message = request.getRequestURI().endsWith("/register")
                ? "Revisa el registro: nombre de 2 a 80 caracteres, correo válido (máximo 120) y contraseña de 8 a 72 caracteres."
                : GENERIC_MESSAGE;
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
