package com.backend.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private int totalWorkouts;
    private int totalMinutes;
    private int avgBpm;
    private List<ChartPointDto> weeklySeries;
    private List<ChartPointDto> bpmSeries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointDto {
        private String label;
        private double value;
    }
}
