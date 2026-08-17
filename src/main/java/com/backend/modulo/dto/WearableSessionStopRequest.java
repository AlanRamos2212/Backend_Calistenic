package com.backend.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WearableSessionStopRequest {
    private OffsetDateTime endedAt;
    private Integer durationSec;
    private Double caloriesBurned;
    private Integer avgHr;
    private Integer maxHr;
    private Integer minHr;
}
