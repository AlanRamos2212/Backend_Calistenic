package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Detail response for `GET /api/routines/{id}`. */
@Value
@Builder
public class RoutineDetailResponse {
    Long id;
    String title;
    String subtitle;
    String category;
    String level;
    Integer durationMin;
    Integer estimatedCalories;
    String coverImageUrl;
    List<ExerciseItem> exercises;

    @Value
    @Builder
    public static class ExerciseItem {
        Long id;
        String name;
        String imageUrl;
        String gifUrl;
        String techniqueDescription;
        String category;
        String level;
        String muscles;
        Integer position;
        Integer sets;
        Integer reps;
        Integer restSeconds;
    }
}
