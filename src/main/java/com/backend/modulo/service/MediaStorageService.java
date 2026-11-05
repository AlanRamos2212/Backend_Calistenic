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
        if (dot >= 0) extension = original.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");
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
}
