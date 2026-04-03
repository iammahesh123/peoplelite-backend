package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSettings extends TenantAwareEntity {

    @Column(name = "pf_enabled")
    @Builder.Default
    private boolean pfEnabled = false;

    @Column(name = "pf_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pfRate = new BigDecimal("12.00");

    @Column(name = "pf_cap", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pfCap = new BigDecimal("1800.00");

    @Column(name = "esi_enabled")
    @Builder.Default
    private boolean esiEnabled = false;

    @Column(name = "esi_employee_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal esiEmployeeRate = new BigDecimal("0.75");

    @Column(name = "esi_employer_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal esiEmployerRate = new BigDecimal("3.25");

    @Column(name = "esi_wage_ceiling", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal esiWageCeiling = new BigDecimal("21000.00");

    @Column(name = "pt_enabled")
    @Builder.Default
    private boolean ptEnabled = false;

    @Column(name = "pt_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal ptAmount = new BigDecimal("200.00");

    @Column(name = "tds_enabled")
    @Builder.Default
    private boolean tdsEnabled = false;

    @Column(name = "pf_employer_enabled")
    @Builder.Default
    private boolean pfEmployerEnabled = false;

    @Column(name = "pf_employer_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pfEmployerRate = new BigDecimal("12.00");

    // ── Overtime Settings ──
    @Column(name = "overtime_enabled")
    @Builder.Default
    private boolean overtimeEnabled = false;

    @Column(name = "overtime_multiplier", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal overtimeMultiplier = new BigDecimal("1.50");

    @Column(name = "standard_hours_per_day", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal standardHoursPerDay = new BigDecimal("8.00");

    // ── Professional Tax Mode ──
    @Column(name = "pt_slab_mode")
    @Builder.Default
    private boolean ptSlabMode = false;

    @Column(name = "pt_state")
    private String ptState;

    // ── TDS Settings ──
    @Column(name = "tds_regime")
    @Builder.Default
    private String tdsRegime = "NEW";

    // ── Email Payslips ──
    @Column(name = "auto_email_payslips")
    @Builder.Default
    private boolean autoEmailPayslips = false;
}
