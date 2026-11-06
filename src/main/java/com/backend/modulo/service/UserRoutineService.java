package com.backend.modulo.service;

import com.backend.modulo.dto.CreateUserRoutineRequest;
import com.backend.modulo.dto.UserRoutineDetailResponse;
import com.backend.modulo.dto.UserRoutineResponse;
import com.backend.modulo.entity.Exercise;
import com.backend.modulo.entity.User;
import com.backend.modulo.entity.UserRoutine;
import com.backend.modulo.entity.UserRoutineExercise;
import com.backend.modulo.repository.ExerciseRepository;
import com.backend.modulo.repository.UserRoutineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Personalized routines owned by the authenticated user. Every read/write is
 * scoped to userId: another user's routine is indistinguishable from a
 * missing one (404), which avoids leaking existence.
 */
@Service
public class UserRoutineService {

    private final UserRoutineRepository userRoutineRepository;
    private final ExerciseRepository exerciseRepository;

    public UserRoutineService(UserRoutineRepository userRoutineRepository,
                              ExerciseRepository exerciseRepository) {
        this.userRoutineRepository = userRoutineRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional(readOnly = true)
    public List<UserRoutineResponse> list(Long userId) {
        return userRoutineRepository.findAllForUser(userId).stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public UserRoutineDetailResponse detail(Long id, Long userId) {
        UserRoutine routine = userRoutineRepository.findDetailById(id)
                .filter(r -> r.getUser().getId().equals(userId))
                .orElseThrow(this::notFound);
        return UserRoutineDetailResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .exercises(routine.getExercises().stream().map(this::toItem).toList())
                .build();
    }

    /** Creates the routine for the given user with 1-based positions by list order. */
    @Transactional
    public UserRoutineDetailResponse create(Long userId, CreateUserRoutineRequest request) {
        UserRoutine routine = UserRoutine.builder()
                .user(User.builder().id(userId).build())
                .title(request.getTitle().trim())
                .build();

        int position = 1;
        for (CreateUserRoutineRequest.ExerciseRequest item : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(item.getExerciseId())
                    .orElseThrow(() -> notFound("Ejercicio " + item.getExerciseId() + " no encontrado."));
            routine.getExercises().add(UserRoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .position(position++)
                    .sets(item.getSets())
                    .reps(item.getReps())
                    .restSeconds(item.getRestSeconds() == null ? 60 : item.getRestSeconds())
                    .build());
        }
        UserRoutine saved = userRoutineRepository.save(routine);
        return toDetail(saved);
    }

    /** True when the routine exists AND belongs to the user. */
    @Transactional(readOnly = true)
    public boolean isOwnedBy(Long id, Long userId) {
        return userRoutineRepository.findDetailById(id)
                .filter(r -> r.getUser().getId().equals(userId))
                .isPresent();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        UserRoutine routine = userRoutineRepository.findDetailById(id)
                .filter(r -> r.getUser().getId().equals(userId))
                .orElseThrow(this::notFound);
        userRoutineRepository.delete(routine);
    }

    private UserRoutineResponse toSummary(UserRoutine routine) {
        return UserRoutineResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .exerciseCount(routine.getExercises().size())
                .totalSets(routine.getExercises().stream()
                        .mapToInt(UserRoutineExercise::getSets).sum())
                .build();
    }

    private UserRoutineDetailResponse toDetail(UserRoutine routine) {
        return UserRoutineDetailResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .exercises(routine.getExercises().stream().map(this::toItem).toList())
                .build();
    }

    private UserRoutineDetailResponse.ExerciseItem toItem(UserRoutineExercise re) {
        Exercise e = re.getExercise();
        return UserRoutineDetailResponse.ExerciseItem.builder()
                .id(e.getId())
                .name(e.getName())
                .imageUrl(e.getImageUrl())
                .gifUrl(e.getGifUrl())
                .description(e.getDescription())
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

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException notFound() {
        return notFound("Rutina no encontrada.");
    }
}
