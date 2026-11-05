package com.backend.modulo.dto;

import com.backend.modulo.entity.WearableBinding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta de vinculación de wearable.
 * Nunca expone el deviceToken ni la entidad User (evita serialización LAZY
 * y fuga del token de dispositivo por el API).
 */
@Getter
@Builder
@AllArgsConstructor
public class WearableBindingResponse {
    private Long id;
    private String wearableId;
    private java.time.OffsetDateTime pairedAt;
    private Boolean active;
    private Boolean pinConfirmed;
    private java.time.OffsetDateTime confirmedAt;
    private String deviceName;

    public static WearableBindingResponse from(WearableBinding b) {
        return WearableBindingResponse.builder()
                .id(b.getId())
                .wearableId(b.getWearableId())
                .pairedAt(b.getPairedAt())
                .active(b.getActive())
                .pinConfirmed(b.getPinConfirmed())
                .confirmedAt(b.getConfirmedAt())
                .build();
    }
}
