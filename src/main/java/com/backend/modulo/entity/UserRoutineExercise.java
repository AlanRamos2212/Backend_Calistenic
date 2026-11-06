package com.backend.modulo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_routine_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoutineExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_routine_id", nullable = false)
    private UserRoutine routine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /** 1-based order inside the routine. */
    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer sets;

    /** Reps; null for time-based (isometric) exercises. */
    private Integer reps;

    @Column(name = "rest_seconds", nullable = false)
    private Integer restSeconds;
}
