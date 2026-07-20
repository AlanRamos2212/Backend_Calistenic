package com.backend.modulo.repository;

import com.backend.modulo.document.RoutineDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio CRUD básico para RoutineDocument. Igual que
 * ExerciseSearchRepository, solo para seeding y operaciones simples;
 * la búsqueda avanzada vive en SearchService.
 */
@Repository
public interface RoutineSearchRepository extends ElasticsearchRepository<RoutineDocument, String> {
}
