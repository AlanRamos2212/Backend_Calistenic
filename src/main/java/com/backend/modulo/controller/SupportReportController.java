package com.backend.modulo.controller;

import com.backend.modulo.dto.CreateSupportReportRequest;
import com.backend.modulo.dto.SupportReportResponse;
import com.backend.modulo.dto.UpdateSupportReportRequest;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.service.SupportReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * Support reports (Practica 7): authenticated users open bug reports;
 * only the ADMIN role (technical, does not create routines) lists and
 * updates their status.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class SupportReportController {
    private final SupportReportService reportService;
    private final UserRepository userRepository;

    /** My own reports (any authenticated user). */
    @GetMapping("/mine")
    public ResponseEntity<List<SupportReportResponse>> mine(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(reportService.listMine(user(principal).getId()));
    }

    /** Admin-only list, optionally filtered by ?status=. */
    @GetMapping
    public ResponseEntity<List<SupportReportResponse>> list(@RequestParam(required = false) String status,
                                                              @AuthenticationPrincipal UserDetails principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reportService.listAll(status));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<SupportReportResponse> create(@Valid @RequestBody CreateSupportReportRequest request,
                                                          @AuthenticationPrincipal UserDetails principal) {
        SupportReportResponse response = reportService.create(user(principal).getId(), request, null);
        return ResponseEntity.created(URI.create("/api/reports/" + response.getId())).body(response);
    }

    /** Multipart create: title + description + optional screenshot. */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SupportReportResponse> createMultipart(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails principal) {
        CreateSupportReportRequest request = CreateSupportReportRequest.builder()
                .title(title).description(description).category(category).build();
        SupportReportResponse response = reportService.create(user(principal).getId(), request, image);
        return ResponseEntity.created(URI.create("/api/reports/" + response.getId())).body(response);
    }

    /** Admin-only status transition (OPEN -> IN_PROGRESS -> RESOLVED). */
    @PatchMapping("/{id}")
    public ResponseEntity<SupportReportResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateSupportReportRequest request,
                                                          @AuthenticationPrincipal UserDetails principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reportService.updateStatus(id, request.getStatus()));
    }

    private boolean isAdmin(UserDetails principal) {
        return principal != null && userRepository.findByEmail(principal.getUsername())
                .map(u -> u.getRole() == User.Role.ADMIN).orElse(false);
    }

    private User user(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
