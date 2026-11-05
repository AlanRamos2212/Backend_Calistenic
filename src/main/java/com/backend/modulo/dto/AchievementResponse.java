package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class AchievementResponse {
    Long id;
    String code;
    String title;
    String description;
    OffsetDateTime unlockedAt;
}
