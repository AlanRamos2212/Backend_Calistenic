package com.backend.modulo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Documento Elasticsearch para rutinas de calistenia (catálogo del sitio web).
 * Índice independiente de "exercises" para poder buscar ambos tipos de
 * contenido y combinarlos en un único resultado de búsqueda global.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "routines")
public class RoutineDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;       // upper_body, core, lower_body, full_body

    @Field(type = FieldType.Keyword)
    private String level;          // principiante, intermedio, avanzado

    @Field(type = FieldType.Keyword)
    private List<String> exerciseIds;

    @Field(type = FieldType.Integer)
    private Integer durationMinutes;

    @Field(type = FieldType.Integer)
    private Integer caloriesEstimate;

    @Field(type = FieldType.Keyword)
    private String hrRange;        // ej. "130-160"
}
