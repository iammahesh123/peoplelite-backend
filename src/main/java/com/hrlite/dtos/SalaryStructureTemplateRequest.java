package com.hrlite.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaryStructureTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    @NotNull(message = "Basic percentage is required")
    @Min(value = 0, message = "Basic percentage must be non-negative")
    private BigDecimal basicPercent;

    @NotNull(message = "HRA percentage is required")
    @Min(value = 0, message = "HRA percentage must be non-negative")
    private BigDecimal hraPercent;

    @NotNull(message = "Special allowance percentage is required")
    @Min(value = 0, message = "Special allowance percentage must be non-negative")
    private BigDecimal specialAllowancePercent;

    private boolean isDefault;
}
