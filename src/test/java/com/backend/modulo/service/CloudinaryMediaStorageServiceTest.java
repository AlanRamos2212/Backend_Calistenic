package com.backend.modulo.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CloudinaryMediaStorageServiceTest {

    @Test
    void extractsPublicIdFromStandardUrl() {
        assertEquals("calistenic/abcdef123456",
                CloudinaryMediaStorageService.publicIdFromUrl(
                        "https://res.cloudinary.com/demo/image/upload/v1720000000/calistenic/abcdef123456.jpg"));
    }

    @Test
    void extractsPublicIdWithoutVersionOrExtension() {
        assertEquals("calistenic/nested/id",
                CloudinaryMediaStorageService.publicIdFromUrl(
                        "https://res.cloudinary.com/demo/image/upload/calistenic/nested/id"));
    }

    @Test
    void ignoresForeignUrls() {
        assertNull(CloudinaryMediaStorageService.publicIdFromUrl(
                "http://localhost:8082/api/media/a1b2c3.jpg"));
        assertNull(CloudinaryMediaStorageService.publicIdFromUrl(null));
        assertNull(CloudinaryMediaStorageService.publicIdFromUrl(""));
    }
}
