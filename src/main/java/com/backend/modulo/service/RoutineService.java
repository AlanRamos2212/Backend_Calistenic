package com.backend.modulo.service;

import com.backend.modulo.dto.*;
import com.backend.modulo.entity.Exercise;
import com.backend.modulo.entity.Routine;
import com.backend.modulo.entity.RoutineExercise;
import com.backend.modulo.repository.ExerciseRepository;
import com.backend.modulo.repository.RoutineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Curated workout routines: list, detail and admin CRUD. Exercises inside a
 * routine keep their DB order via {@code position} (1-based, assigned by list order).
 */
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;

    public RoutineService(RoutineRepository routineRepository, ExerciseRepository exerciseRepository) {
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional(readOnly = true)
    public List<RoutineResponse> list(String category) {
        List<Routine> routines = (category == null || category.isBlank())
                ? routineRepository.findAllWithExercises()
                : routineRepository.findByCategory(category.trim());
        return routines.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public RoutineDetailResponse detail(Long id) {
        Routine routine = routineRepository.findDetailById(id)
                .orElseThrow(() -> notFound("Rutina no encontrada."));
        return RoutineDetailResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .subtitle(routine.getSubtitle())
                .category(routine.getCategory())
                .level(routine.getLevel())
                .durationMin(routine.getDurationMin())
                .estimatedCalories(routine.getEstimatedCalories())
                .coverImageUrl(routine.getCoverImageUrl())
                .exercises(routine.getExercises().stream().map(this::toItem).toList())
                .build();
    }

    /** Creates a routine; exercises are stored in the order they arrive (1-based positions). */
    @Transactional
    public RoutineResponse create(CreateRoutineRequest request) {
        Routine routine = Routine.builder()
                .title(request.getTitle().trim())
                .subtitle(trimOrNull(request.getSubtitle()))
                .category(request.getCategory().trim())
                .level(request.getLevel().trim())
                .durationMin(request.getDurationMin())
                .estimatedCalories(request.getEstimatedCalories())
                .coverImageUrl(trimOrNull(request.getCoverImageUrl()))
                .build();

        int position = 1;
        for (CreateRoutineRequest.ExerciseRequest item : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(item.getExerciseId())
                    .orElseThrow(() -> notFound("Ejercicio " + item.getExerciseId() + " no encontrado."));
            routine.getExercises().add(RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .position(position++)
                    .sets(item.getSets())
                    .reps(item.getReps())
                    .restSeconds(item.getRestSeconds())
                    .build());
        }
        return toSummary(routineRepository.save(routine));
    }

    /** Deletes a routine; routine_exercises rows cascade. */
    @Transactional
    public void delete(Long id) {
        if (!routineRepository.existsById(id)) throw notFound("Rutina no encontrada.");
        routineRepository.deleteById(id);
    }

    private RoutineResponse toSummary(Routine routine) {
        return RoutineResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .subtitle(routine.getSubtitle())
                .category(routine.getCategory())
                .level(routine.getLevel())
                .durationMin(routine.getDurationMin())
                .estimatedCalories(routine.getEstimatedCalories())
                .coverImageUrl(routine.getCoverImageUrl())
                .exerciseCount(routine.getExercises().size())
                .build();
    }

    private RoutineDetailResponse.ExerciseItem toItem(RoutineExercise re) {
        Exercise e = re.getExercise();
        return RoutineDetailResponse.ExerciseItem.builder()
                .id(e.getId())
                .name(e.getName())
                .imageUrl(e.getImageUrl())
                .gifUrl(e.getGifUrl())
                .techniqueDescription(e.getTechniqueDescription())
                .category(e.getCategory())
                .level(e.getLevel())
                .muscles(e.getMuscles())
                .estimatedCalories(e.getEstimatedCalories())
                .position(re.getPosition())
                .sets(re.getSets())
                .reps(re.getReps())
                .restSeconds(re.getRestSeconds())
                .build();
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
