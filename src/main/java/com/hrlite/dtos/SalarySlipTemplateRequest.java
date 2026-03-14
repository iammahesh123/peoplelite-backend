package com.hrlite.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalarySlipTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    @NotBlank(message = "HTML content is required")
    private String htmlContent;

    private String description;

    private boolean isDefault = false;
}
