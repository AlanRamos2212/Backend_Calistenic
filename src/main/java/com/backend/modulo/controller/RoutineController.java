package com.backend.modulo.controller;

import com.backend.modulo.dto.CreateRoutineRequest;
import com.backend.modulo.dto.RoutineDetailResponse;
import com.backend.modulo.dto.RoutineResponse;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.service.RoutineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;
    private final UserRepository userRepository;

    /** Public list; optional ?category= filter (e.g. upper_body, core, full_body). */
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> list(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(routineService.list(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(routineService.detail(id));
    }

    /** Admin-only: creates a routine with ordered exercises. */
    @PostMapping
    public ResponseEntity<RoutineResponse> create(@Valid @RequestBody CreateRoutineRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        requireAdmin(principal);
        RoutineResponse response = routineService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Admin-only: deletes a routine (cascade rows). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        requireAdmin(principal);
        routineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        if (user.getRole() != User.Role.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Solo un administrador puede modificar rutinas.");
        }
    }
}
