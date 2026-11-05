package com.backend.modulo.controller;

import com.backend.modulo.entity.Exercise;
import com.backend.modulo.repository.ExerciseRepository;
import com.backend.modulo.service.MediaStorage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final MediaStorage mediaStorageService;

    @GetMapping
    public ResponseEntity<List<Exercise>> listAll() {
        return ResponseEntity.ok(exerciseRepository.findAll());
    }

    /** Full exercise detail: media paths plus technique description. */
    @GetMapping("/{id}")
    public ResponseEntity<Exercise> detail(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejercicio no encontrado.")));
    }

    @PostMapping("/{id}/media")
    public ResponseEntity<Exercise> uploadMedia(
            @PathVariable Long id,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) MultipartFile gif,
            @RequestPart(required = false) String techniqueDescription,
            @AuthenticationPrincipal UserDetails principal) throws Exception {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (principal.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede cargar multimedia.");
        }
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejercicio no encontrado."));
        if (image != null && !image.isEmpty()) exercise.setImageUrl(mediaStorageService.store(image));
        if (gif != null && !gif.isEmpty()) exercise.setGifUrl(mediaStorageService.store(gif));
        if (techniqueDescription != null) exercise.setTechniqueDescription(techniqueDescription.trim());
        return ResponseEntity.ok(exerciseRepository.save(exercise));
    }
}
