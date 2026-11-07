package com.backend.modulo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSupportReportRequest {
    @NotBlank(message = "El título no puede estar vacío.")
    @Size(max = 120)
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía.")
    @Size(max = 4000)
    private String description;

    @Pattern(regexp = "CRASH|UI|DATOS|RED|OTRO")
    private String category;
}
