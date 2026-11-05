package com.backend.modulo.controller;

import com.backend.modulo.service.LocalMediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves locally stored uploads. With the Cloudinary strategy active, assets
 * live on the CDN and this controller responds 404 (no local bean).
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    private final ObjectProvider<LocalMediaStorageService> mediaStorageProvider;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String filename) throws Exception {
        LocalMediaStorageService mediaStorageService = mediaStorageProvider.getIfAvailable();
        if (mediaStorageService == null) return ResponseEntity.notFound().build();

        Path path = mediaStorageService.resolve(filename);
        if (path == null || !Files.exists(path)) return ResponseEntity.notFound().build();
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
