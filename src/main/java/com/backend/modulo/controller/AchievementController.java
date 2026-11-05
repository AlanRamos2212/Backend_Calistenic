package com.backend.modulo.controller;

import com.backend.modulo.dto.AchievementResponse;
import com.backend.modulo.entity.User;
import com.backend.modulo.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;
    private final com.backend.modulo.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AchievementResponse>> mine(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(achievementService.findForUser(user(principal).getId()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AchievementResponse>> byUser(@PathVariable Long userId,
                                                              @AuthenticationPrincipal UserDetails principal) {
        if (!user(principal).getId().equals(userId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(achievementService.findForUser(userId));
    }

    /**
     * Desbloqueo manual de logros restringido a ADMIN.
     * Los logros normales se desbloquean con el trigger de BD
     * ({@code unlock_training_achievements}, migración V6), no por API.
     */
    @PostMapping("/{code}/unlock")
    public ResponseEntity<AchievementResponse> unlock(@PathVariable String code,
                                                       @AuthenticationPrincipal UserDetails principal) {
        User user = user(principal);
        if (!"ADMIN".equalsIgnoreCase(user.getRole().name()))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(achievementService.unlock(user.getId(), code));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        achievementService.delete(user(principal).getId(), id);
        return ResponseEntity.noContent().build();
    }

    private User user(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
