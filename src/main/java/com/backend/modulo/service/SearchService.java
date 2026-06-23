package com.backend.modulo.service;

import com.backend.modulo.document.ExerciseDocument;
import com.backend.modulo.dto.SearchResultDto;
import com.backend.modulo.repository.ExerciseSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ExerciseSearchRepository exerciseRepository;

    public List<SearchResultDto> searchExercises(String query) {
        // Find matching exercises
        List<ExerciseDocument> results = exerciseRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);

        // Map to SearchResultDto for Angular
        return results.stream()
                .map(doc -> SearchResultDto.builder()
                        .title(doc.getName())
                        .description(doc.getDescription())
                        .category("Ejercicio") // All from DB are 'Ejercicio' category
                        .icon(doc.getIcon())
                        .routerLink(List.of("/ejercicios"))
                        .build())
                .collect(Collectors.toList());
    }
}
