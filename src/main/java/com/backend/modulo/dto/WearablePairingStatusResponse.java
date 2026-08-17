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
public class WearablePairingStatusResponse {
    private String code;
    private String status;
    private String wearableId;
    private Boolean pinConfirmed;
    private OffsetDateTime expiresAt;
}
