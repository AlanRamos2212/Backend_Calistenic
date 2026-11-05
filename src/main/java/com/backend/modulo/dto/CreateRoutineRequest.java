package com.backend.modulo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/** Admin request body for `POST /api/routines`. */
@Data
public class CreateRoutineRequest {
    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 200)
    private String subtitle;

    @NotBlank
    @Size(max = 50)
    private String category;

    @NotBlank
    @Size(max = 50)
    private String level;

    private Integer durationMin;
    private Integer estimatedCalories;
    private String coverImageUrl;

    @Valid
    @NotEmpty
    private List<ExerciseRequest> exercises;

    @Data
    public static class ExerciseRequest {
        @NotNull
        private Long exerciseId;

        @Min(1)
        @Max(20)
        private int sets = 3;

        private Integer reps;

        @Min(0)
        @Max(600)
        private Integer restSeconds = 60;
    }
}
