package com.backend.modulo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routine_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /** 1-based position of the exercise inside the routine. */
    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer sets;

    /** Number of reps; null when the exercise is time-based (e.g. plank). */
    private Integer reps;

    @Column(name = "rest_seconds")
    private Integer restSeconds;
}
