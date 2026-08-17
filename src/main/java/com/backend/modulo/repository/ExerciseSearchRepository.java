package com.backend.modulo.repository;

import com.backend.modulo.document.ExerciseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Repositorio CRUD básico para ExerciseDocument.
 *
 * Se usa para operaciones simples: guardar el seed data
 * (DataInitializerService),
 * contar documentos, buscar por id. La búsqueda avanzada con fuzzy matching,
 * boost de relevancia y filtros combinados NO vive aquí: se implementa en
 * SearchService con ElasticsearchOperations + NativeQuery, porque las
 * "derived queries" (findByXContaining...) no permiten construir queries
 * bool con fuzziness ni combinar "should" (texto) con "filter" (exactos).
 */
public interface ExerciseSearchRepository extends ElasticsearchRepository<ExerciseDocument, String> {
}
