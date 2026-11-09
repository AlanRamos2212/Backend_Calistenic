package com.backend.modulo.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UserListResponse {
    Long id;
    String name;
    String email;
    String role;
    LocalDateTime createdAt;
    String avatarUrl;
}
