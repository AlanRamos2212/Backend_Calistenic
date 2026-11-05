package com.backend.modulo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Storage abstraction for user uploads (post images, avatars, exercise media).
 *
 * Implementations:
 * - {@link LocalMediaStorageService}: files in {@code uploads/media} (dev/Docker).
 * - {@link CloudinaryMediaStorageService}: Cloudinary CDN via direct HTTP (prod/Render).
 */
public interface MediaStorage {

    /**
     * Stores the file and returns the ready-to-persist media path:
     * - Local strategy returns a relative path such as {@code /api/media/<uuid>.jpg}.
     * - Cloudinary strategy returns an absolute CDN URL.
     */
    String store(MultipartFile file) throws IOException;

    /**
     * Deletes the asset addressed by a previously returned media path.
     *
     * @return true when an asset was actually deleted, false when it simply
     *         did not exist or the URL does not belong to this storage
     */
    boolean delete(String mediaUrl) throws IOException;
}
