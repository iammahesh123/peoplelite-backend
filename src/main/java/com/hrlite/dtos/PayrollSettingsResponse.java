package com.hrlite.dtos;

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
public class PayrollSettingsResponse {
    private UUID id;

    private boolean pfEnabled;
    private BigDecimal pfRate;
    private BigDecimal pfCap;

    private boolean esiEnabled;
    private BigDecimal esiEmployeeRate;
    private BigDecimal esiEmployerRate;
    private BigDecimal esiWageCeiling;

    private boolean ptEnabled;
    private BigDecimal ptAmount;

    private boolean tdsEnabled;

    private boolean pfEmployerEnabled;
    private BigDecimal pfEmployerRate;

    // Overtime
    private boolean overtimeEnabled;
    private BigDecimal overtimeMultiplier;
    private BigDecimal standardHoursPerDay;

    // PT slab mode
    private boolean ptSlabMode;
    private String ptState;

    // TDS
    private String tdsRegime;

    // Email
    private boolean autoEmailPayslips;
}
