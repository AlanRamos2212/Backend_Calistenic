package com.backend.modulo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateSupportReportRequest {
    @NotBlank
    @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED")
    private String status;
}
