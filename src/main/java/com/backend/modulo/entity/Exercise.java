package com.backend.modulo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String level;

    private Integer durationMin;
    private Integer estimatedCalories;
    private Integer targetHrMin;
    private Integer targetHrMax;
    private String icon;
    private Integer exerciseCount;
    @Column(length = 500)
    private String muscles; // stored as comma-separated
}
