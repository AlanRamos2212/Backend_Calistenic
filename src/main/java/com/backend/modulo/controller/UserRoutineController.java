package com.backend.modulo.controller;

import com.backend.modulo.dto.CreateUserRoutineRequest;
import com.backend.modulo.dto.UserRoutineDetailResponse;
import com.backend.modulo.dto.UserRoutineResponse;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.service.UserRoutineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** `mis rutinas`: CRUD scoped to the authenticated user. */
@RestController
@RequestMapping("/api/user-routines")
@RequiredArgsConstructor
public class UserRoutineController {

    private final UserRoutineService userRoutineService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserRoutineResponse>> list(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userRoutineService.list(user(principal).getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRoutineDetailResponse> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userRoutineService.detail(id, user(principal).getId()));
    }

    @PostMapping
    public ResponseEntity<UserRoutineDetailResponse> create(
            @Valid @RequestBody CreateUserRoutineRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        UserRoutineDetailResponse response =
                userRoutineService.create(user(principal).getId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        userRoutineService.delete(id, user(principal).getId());
        return ResponseEntity.noContent().build();
    }

    private com.backend.modulo.entity.User user(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
