package com.backend.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filtros opcionales que el buscador de Angular puede enviar junto al
 * término de búsqueda. Todos son opcionales: si vienen nulos, no se aplican.
 *
 * Soporta la sintaxis "campo:valor" documentada en la Práctica 6
 * (ej. "nivel:avanzado", "musculo:triceps") que el SearchBarComponent
 * de Angular parsea antes de llamar a la API.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilters {
    private String level;          // principiante | intermedio | avanzado
    private String category;       // upper_body | lower_body | core | full_body
    private List<String> muscles;  // ej. ["Tríceps", "Pecho"]
    private String type;           // "exercise" | "routine" | null (ambos)
}
