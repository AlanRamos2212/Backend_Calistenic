package com.backend.modulo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "wearable_bindings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WearableBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wearable_id", nullable = false)
    private String wearableId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", nullable = false, length = 2048)
    private String deviceToken;

    @Column(name = "paired_at", nullable = false)
    private OffsetDateTime pairedAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "pin_confirmed", nullable = false)
    @Builder.Default
    private Boolean pinConfirmed = false;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @PrePersist
    void onCreate() {
        if (pairedAt == null) {
            pairedAt = OffsetDateTime.now();
        }
    }
}
