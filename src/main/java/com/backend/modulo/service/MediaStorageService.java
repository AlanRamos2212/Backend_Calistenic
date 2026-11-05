package com.backend.modulo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class MediaStorageService {
    private final Path root;

    public MediaStorageService(@Value("${app.media.storage-dir:uploads/media}") String storageDir) throws IOException {
        root = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            extension = original.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");
        } else if (file.getContentType() != null) {
            String ct = file.getContentType().toLowerCase();
            if (ct.contains("png")) extension = ".png";
            else if (ct.contains("webp")) extension = ".webp";
            else if (ct.contains("gif")) extension = ".gif";
            else extension = ".jpg";
        } else {
            extension = ".jpg";
        }
        String filename = UUID.randomUUID() + extension.toLowerCase();
        Path destination = root.resolve(filename).normalize();
        if (!destination.startsWith(root)) throw new IOException("Ruta de archivo no válida.");
        Files.copy(file.getInputStream(), destination);
        return filename;
    }

    public Path resolve(String filename) {
        Path path = root.resolve(filename).normalize();
        return path.startsWith(root) ? path : null;
    }

    /**
     * Deletes a locally stored media file referenced by an API URL.
     *
     * @return true when a file was deleted, false when there was no local file
     */
    public boolean deleteIfExists(String mediaUrl) throws IOException {
        if (mediaUrl == null || mediaUrl.isBlank()) return false;

        String filename = mediaUrl;
        String marker = "/api/media/";
        int markerIndex = mediaUrl.indexOf(marker);
        if (markerIndex >= 0) {
            filename = mediaUrl.substring(markerIndex + marker.length());
        }

        Path file = resolve(filename);
        if (file == null || filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        return Files.deleteIfExists(file);
    }
}
