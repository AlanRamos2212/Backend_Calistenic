package com.backend.modulo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "wearable_session_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WearableSessionPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private WearableSession session;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "heart_rate")
    private Integer heartRate;

    private Integer cadence;

    private Double distance;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private Map<String, Object> extraData;
}
