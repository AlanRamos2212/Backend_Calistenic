package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExerciseMediaResponse {
    Long exerciseId;
    String imageUrl;
    String gifUrl;
    String techniqueDescription;
}
