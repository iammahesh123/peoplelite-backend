package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRevisionResponse {

    private UUID id;
    private UUID employeeId;
    private BigDecimal previousCTC;
    private BigDecimal newCTC;
    private BigDecimal previousBasic;
    private BigDecimal newBasic;
    private LocalDate effectiveDate;
    private String reason;
    private String remarks;
    private UUID revisedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
