package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Detail response for `GET /api/user-routines/{id}`. */
@Value
@Builder
public class UserRoutineDetailResponse {
    Long id;
    String title;
    List<ExerciseItem> exercises;

    @Value
    @Builder
    public static class ExerciseItem {
        Long id;
        String name;
        String imageUrl;
        String gifUrl;
        String description;
        String techniqueDescription;
        String category;
        String level;
        String muscles;
        Integer estimatedCalories;
        Integer position;
        Integer sets;
        Integer reps;
        Integer restSeconds;
    }
}
