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
public class WearablePairingCodeResponse {
    private String code;
    private OffsetDateTime expiresAt;
    private long expiresInSeconds;
}
