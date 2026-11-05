package com.backend.modulo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePostRequest {
    @NotBlank
    @Size(max = 4000)
    private String content;

    private Long workoutSessionId;
}
