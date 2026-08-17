package com.backend.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WearableSessionMetricsRequest {
    private OffsetDateTime timestamp;
    private Integer heartRate;
    private Integer cadence;
    private Double distance;
    private Map<String, Object> extraData;
}
