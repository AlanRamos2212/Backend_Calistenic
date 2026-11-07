package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class SupportReportResponse {
    Long id;
    Long userId;
    String userName;
    String title;
    String description;
    String category;
    String imageUrl;
    String status;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
