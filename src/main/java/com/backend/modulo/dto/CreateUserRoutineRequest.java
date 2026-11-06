package com.backend.modulo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** Request body for `POST /api/user-routines`. */
@Data
public class CreateUserRoutineRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

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
