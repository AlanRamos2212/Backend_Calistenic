package com.backend.modulo.service;

import com.backend.modulo.dto.AchievementResponse;
import com.backend.modulo.entity.Achievement;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.AchievementRepository;
import com.backend.modulo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AchievementResponse> findForUser(Long userId) {
        return achievementRepository.findByUser_IdOrderByUnlockedAtDesc(userId).stream().map(this::response).toList();
    }

    @Transactional
    public AchievementResponse unlock(Long userId, String code) {
        String normalized = code.trim().toUpperCase();
        return achievementRepository.findByUser_IdAndCode(userId, normalized)
                .map(this::response)
                .orElseGet(() -> response(achievementRepository.save(Achievement.builder()
                        .user(userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.")))
                        .code(normalized)
                        .title(titleFor(normalized))
                        .description(descriptionFor(normalized))
                        .build())));
    }

    @Transactional
    public void delete(Long userId, Long achievementId) {
        Achievement achievement = achievementRepository.findByIdAndUser_Id(achievementId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado."));
        achievementRepository.delete(achievement);
    }

    private AchievementResponse response(Achievement achievement) {
        return AchievementResponse.builder().id(achievement.getId()).code(achievement.getCode())
                .title(achievement.getTitle()).description(achievement.getDescription())
                .unlockedAt(achievement.getUnlockedAt()).build();
    }

    private String titleFor(String code) {
        return switch (code) {
            case "FIRST_WORKOUT" -> "Primer entrenamiento";
            case "FIRE_WEEK" -> "Semana de fuego";
            case "100_WORKOUTS" -> "100 entrenamientos";
            default -> code;
        };
    }

    private String descriptionFor(String code) {
        return switch (code) {
            case "FIRST_WORKOUT" -> "Completaste tu primera sesión de entrenamiento.";
            case "FIRE_WEEK" -> "Entrenaste durante siete días consecutivos.";
            case "100_WORKOUTS" -> "Completaste 100 sesiones de entrenamiento.";
            default -> "Logro desbloqueado en CalistenicTrack.";
        };
    }
}
