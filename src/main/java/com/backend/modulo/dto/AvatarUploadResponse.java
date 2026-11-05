package com.backend.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadResponse {
    /** Relative filename stored in the media folder (e.g. "abc123.jpg"). */
    private String filename;
    /** Full URL to fetch the avatar via GET /api/media/{filename}. */
    private String avatarUrl;
}
