package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollRunResponse {
    private UUID id;
    private int month;
    private int year;
    private String status;
    private LocalDate runDate;
    private BigDecimal totalGross;
    private BigDecimal totalDeductions;
    private BigDecimal totalNet;
    private int employeeCount;
    private LocalDateTime createdAt;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private String reversalReason;
    private List<PayslipResponse> payslips;
}
