package com.hrlite.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRevisionRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    private BigDecimal previousCTC;

    @NotNull(message = "New CTC is required")
    private BigDecimal newCTC;

    private BigDecimal previousBasic;

    private BigDecimal newBasic;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private String reason;

    private String remarks;
}
