package com.backend.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String id;
    private String title;
    private String description;
    private String category;       // "Ejercicio" | "Rutina" (mostrado en UI)
    private String type;           // "exercise" | "routine" (uso interno/routing)
    private String icon;
    private String level;
    private List<String> muscles;
    private List<String> routerLink;
    private Double score;          // relevancia de Elasticsearch, útil para depurar el ranking
}
