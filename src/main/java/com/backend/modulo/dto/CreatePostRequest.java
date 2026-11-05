package com.backend.modulo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {
    @NotBlank
    @Size(max = 4000)
    private String content;

    private Long workoutSessionId;
}
