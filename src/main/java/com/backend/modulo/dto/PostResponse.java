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
    /** Small portrait shown alongside the post; null renders initial-based avatar. */
    String userAvatar;
    /** Optional attached image served by /api/media; null for text-only posts. */
    String imageUrl;
    Long workoutSessionId;
    long likesCount;
    long commentsCount;
    boolean likedByCurrentUser;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
