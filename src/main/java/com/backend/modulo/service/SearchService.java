package com.backend.modulo.service;

import com.backend.modulo.document.ExerciseDocument;
import com.backend.modulo.document.RoutineDocument;
import com.backend.modulo.dto.SearchFilters;
import com.backend.modulo.dto.SearchResultDto;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de búsqueda avanzada sobre Elasticsearch.
 *
 * Implementa tres capacidades que las "derived queries" de Spring Data
 * (findByNameContaining...) NO pueden ofrecer:
 *
 *  1. FUZZY MATCHING: tolera errores de tipeo ("pus-ups" encuentra "push-ups")
 *     mediante fuzziness("AUTO"), que ajusta la distancia de Levenshtein
 *     permitida según la longitud del término.
 *
 *  2. RELEVANCIA POR BOOST: el campo "name" pesa más que "description"
 *     en el ranking de resultados (boost 3x vs 1x), porque un match en el
 *     nombre del ejercicio es más relevante que uno en su descripción.
 *
 *  3. FILTROS COMBINADOS: una query "bool" separa claramente las cláusulas
 *     de texto (should, afectan el score) de las cláusulas de filtro
 *     (filter, no afectan el score pero sí acotan resultados) — soportando
 *     la sintaxis "nivel:avanzado" o "musculo:triceps" documentada en la
 *     Práctica 6.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final double MIN_SCORE = 0.1;

    public List<SearchResultDto> search(String rawQuery, SearchFilters filters) {
        ParsedQuery parsed = parseFieldSyntax(rawQuery, filters);

        List<SearchResultDto> results = new ArrayList<>();

        if (parsed.filters.getType() == null || "exercise".equals(parsed.filters.getType())) {
            results.addAll(searchExercises(parsed.term, parsed.filters));
        }
        if (parsed.filters.getType() == null || "routine".equals(parsed.filters.getType())) {
            results.addAll(searchRoutines(parsed.term, parsed.filters));
        }

        // Combina ambos índices y reordena por score global, mayor relevancia primero
        return results.stream()
                .sorted(Comparator.comparing(SearchResultDto::getScore).reversed())
                .collect(Collectors.toList());
    }

    // ── Búsqueda en índice "exercises" ──────────────────────────────────────
    private List<SearchResultDto> searchExercises(String term, SearchFilters f) {
        Query boolQuery = buildBoolQuery(
                term,
                f,
                List.of("name^3", "description^1"), // boost: nombre pesa 3x más que descripción
                "category",
                "level",
                "muscles"
        );

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(boolQuery)
                .withMinScore((float) MIN_SCORE)
                .withMaxResults(20)
                .build();

        SearchHits<ExerciseDocument> hits = elasticsearchOperations.search(query, ExerciseDocument.class);

        return hits.getSearchHits().stream()
                .map(this::toExerciseDto)
                .collect(Collectors.toList());
    }

    // ── Búsqueda en índice "routines" ───────────────────────────────────────
    private List<SearchResultDto> searchRoutines(String term, SearchFilters f) {
        Query boolQuery = buildBoolQuery(
                term,
                f,
                List.of("name^3", "description^1"),
                "category",
                "level",
                null // las rutinas no tienen campo "muscles" directo
        );

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(boolQuery)
                .withMinScore((float) MIN_SCORE)
                .withMaxResults(20)
                .build();

        SearchHits<RoutineDocument> hits = elasticsearchOperations.search(query, RoutineDocument.class);

        return hits.getSearchHits().stream()
                .map(this::toRoutineDto)
                .collect(Collectors.toList());
    }

    /**
     * Construye la query bool combinando:
     *  - should: multi_match con fuzziness AUTO sobre los campos de texto (afecta score)
     *  - filter: term queries exactas sobre category/level/muscles (no afecta score)
     *
     * minimumShouldMatch(1) asegura que al menos una cláusula "should" coincida
     * cuando hay término de búsqueda; si el término viene vacío (solo filtros,
     * ej. "nivel:avanzado" sin texto), se omite el bloque "should" por completo.
     */
    private Query buildBoolQuery(String term, SearchFilters f, List<String> boostedFields,
                                  String categoryField, String levelField, String musclesField) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (StringUtils.hasText(term)) {
            bool.should(s -> s.multiMatch(m -> m
                    .query(term)
                    .fields(boostedFields)
                    .fuzziness("AUTO")          // tolera typos: "pus-ups" -> "push-ups"
                    .prefixLength(2)            // no aplica fuzzy a los primeros 2 caracteres (más preciso)
            ));
            bool.minimumShouldMatch("1");
        }

        if (f.getCategory() != null) {
            bool.filter(flt -> flt.term(t -> t.field(categoryField).value(f.getCategory())));
        }
        if (f.getLevel() != null) {
            bool.filter(flt -> flt.term(t -> t.field(levelField).value(f.getLevel())));
        }
        if (musclesField != null && f.getMuscles() != null && !f.getMuscles().isEmpty()) {
            for (String muscle : f.getMuscles()) {
                bool.filter(flt -> flt.term(t -> t.field(musclesField).value(muscle)));
            }
        }

        return new Query.Builder().bool(bool.build()).build();
    }

    // ── Mapeo a DTO con score de relevancia ─────────────────────────────────
    private SearchResultDto toExerciseDto(SearchHit<ExerciseDocument> hit) {
        ExerciseDocument doc = hit.getContent();
        return SearchResultDto.builder()
                .id(doc.getId())
                .title(doc.getName())
                .description(doc.getDescription())
                .category("Ejercicio")
                .type("exercise")
                .icon(doc.getIcon())
                .level(doc.getLevel())
                .muscles(doc.getMuscles())
                .routerLink(List.of("/ejercicios", doc.getId()))
                .score((double) hit.getScore())
                .build();
    }

    private SearchResultDto toRoutineDto(SearchHit<RoutineDocument> hit) {
        RoutineDocument doc = hit.getContent();
        return SearchResultDto.builder()
                .id(doc.getId())
                .title(doc.getName())
                .description(doc.getDescription())
                .category("Rutina")
                .type("routine")
                .icon("🏋️")
                .level(doc.getLevel())
                .routerLink(List.of("/rutinas", doc.getId()))
                .score((double) hit.getScore())
                .build();
    }

    /**
     * Parsea la sintaxis "campo:valor" del término de búsqueda, ej:
     *   "push ups nivel:avanzado"        -> term="push ups", level="avanzado"
     *   "musculo:triceps"                -> term="",          muscles=["triceps"]
     *
     * Los filtros explícitos recibidos por parámetro (SearchFilters f) tienen
     * prioridad sobre los detectados en el texto si ambos vienen presentes.
     */
    private ParsedQuery parseFieldSyntax(String rawQuery, SearchFilters existing) {
        SearchFilters.SearchFiltersBuilder builder = existing.toBuilder();
        StringBuilder remaining = new StringBuilder();

        for (String token : rawQuery.trim().split("\\s+")) {
            if (token.startsWith("nivel:") && existing.getLevel() == null) {
                builder.level(token.substring(6).toLowerCase());
            } else if (token.startsWith("musculo:") && (existing.getMuscles() == null || existing.getMuscles().isEmpty())) {
                builder.muscles(List.of(capitalize(token.substring(8))));
            } else if (token.startsWith("categoria:") && existing.getCategory() == null) {
                builder.category(token.substring(10).toLowerCase());
            } else {
                remaining.append(token).append(" ");
            }
        }

        return new ParsedQuery(remaining.toString().trim(), builder.build());
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private record ParsedQuery(String term, SearchFilters filters) {}
}
