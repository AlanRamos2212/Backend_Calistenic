package com.backend.modulo.dto;

import com.backend.modulo.entity.WearableSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WearableSessionResponse {
    private Long id;
    private Long userId;
    private Long exerciseId;
    private String exerciseName;
    private String wearableId;
    private WearableSession.Status status;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private Integer durationSec;
    private Double caloriesBurned;
    private Integer avgHr;
    private Integer maxHr;
    private Integer minHr;
}
