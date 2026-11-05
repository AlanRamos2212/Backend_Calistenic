package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

/** Summary card for `GET /api/routines`. */
@Value
@Builder
public class RoutineResponse {
    Long id;
    String title;
    String subtitle;
    String category;
    String level;
    Integer durationMin;
    Integer estimatedCalories;
    String coverImageUrl;
    int exerciseCount;
}
