package com.backend.modulo.controller;

import com.backend.modulo.dto.SearchFilters;
import com.backend.modulo.dto.SearchResultDto;
import com.backend.modulo.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de búsqueda consumido por SearchBarComponent (Angular).
 *
 * Ejemplos:
 *   GET /api/search?q=push ups
 *   GET /api/search?q=dominadas&level=intermedio
 *   GET /api/search?q=nivel:avanzado          (sintaxis campo:valor en el propio texto)
 *   GET /api/search?q=&muscles=Tríceps,Pecho  (solo filtros, sin texto)
 *
 * CORS ya se gestiona globalmente en application.properties
 * (spring.web.cors.allowed-origins), por lo que no se repite aquí con @CrossOrigin.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public List<SearchResultDto> search(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> muscles,
            @RequestParam(required = false) String type
    ) {
        SearchFilters filters = SearchFilters.builder()
                .level(level)
                .category(category)
                .muscles(muscles)
                .type(type)
                .build();

        return searchService.search(query, filters);
    }
}
