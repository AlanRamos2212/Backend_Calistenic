package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class CommentResponse {
    Long id;
    Long postId;
    Long userId;
    String userName;
    String content;
    /** Portrait of the commenter; null renders initial-based avatar. */
    String userAvatar;
    OffsetDateTime createdAt;
}
