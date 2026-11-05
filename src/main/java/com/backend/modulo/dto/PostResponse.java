package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class PostResponse {
    Long id;
    Long userId;
    String userName;
    String content;
    Long workoutSessionId;
    long likesCount;
    long commentsCount;
    boolean likedByCurrentUser;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
