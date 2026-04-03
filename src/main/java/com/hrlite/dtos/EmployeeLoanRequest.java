package com.hrlite.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeLoanRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotBlank(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Principal amount is required")
    @Min(value = 1, message = "Principal amount must be greater than 0")
    private BigDecimal principalAmount;

    @NotNull(message = "EMI amount is required")
    @Min(value = 1, message = "EMI amount must be greater than 0")
    private BigDecimal emiAmount;

    @NotNull(message = "Start month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    private Integer startMonth;

    @NotNull(message = "Start year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    private Integer startYear;

    private String remarks;
}
