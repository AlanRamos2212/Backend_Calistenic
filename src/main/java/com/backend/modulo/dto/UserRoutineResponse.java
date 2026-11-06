package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

/** Summary card for `GET /api/user-routines`. */
@Value
@Builder
public class UserRoutineResponse {
    Long id;
    String title;
    int exerciseCount;

    /** Total sets across the routine, for quick stats in the app. */
    int totalSets;
}
