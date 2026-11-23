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
 * Documento Elasticsearch para ejercicios de calistenia.
 *
 * Mapeo de tipos:
 *  - Text + analyzer "standard": tokeniza y permite fuzzy matching / relevancia (name, description)
 *  - Keyword: valor exacto, usado para filtros y agregaciones (category, level, muscles)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** createIndex=false evita el HEAD /exercises del arranque; el indice ya existe en Elastic Cloud. */
@Document(indexName = "exercises", createIndex = false)
public class ExerciseDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;   // upper_body, lower_body, core, full_body

    @Field(type = FieldType.Keyword)
    private String level;      // principiante, intermedio, avanzado

    @Field(type = FieldType.Keyword)
    private String icon;

    @Field(type = FieldType.Keyword)
    private List<String> muscles;
}
