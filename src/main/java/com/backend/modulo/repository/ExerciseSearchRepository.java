package com.backend.modulo.repository;

import com.backend.modulo.document.ExerciseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseSearchRepository extends ElasticsearchRepository<ExerciseDocument, String> {
    
    // Custom query methods can be defined here, e.g. finding by name or description
    List<ExerciseDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
