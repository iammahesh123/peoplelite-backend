package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payslips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip extends TenantAwareEntity {

    @Column(name = "payroll_run_id", nullable = false)
    private UUID payrollRunId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "gross_earnings", precision = 12, scale = 2)
    private BigDecimal grossEarnings;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", precision = 12, scale = 2)
    private BigDecimal netPay;

    @Column(name = "basic", precision = 12, scale = 2)
    private BigDecimal basic;

    @Column(name = "hra", precision = 12, scale = 2)
    private BigDecimal hra;

    @Column(name = "special_allowance", precision = 12, scale = 2)
    private BigDecimal specialAllowance;

    @Column(name = "pf_employee", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pfEmployee = BigDecimal.ZERO;

    @Column(name = "professional_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal professionalTax = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "deduction_remarks")
    private String deductionRemarks;

    @Column(name = "working_days")
    private int workingDays;

    @Column(name = "lop_days")
    @Builder.Default
    private double lopDays = 0;

    @Column(name = "esi_employee", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal esiEmployee = BigDecimal.ZERO;

    @Column(name = "esi_employer", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal esiEmployer = BigDecimal.ZERO;

    @Column(name = "pf_employer", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pfEmployer = BigDecimal.ZERO;

    @Column(name = "tds", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tds = BigDecimal.ZERO;

    @Column(name = "pdf_storage_key")
    private String pdfStorageKey;

    @Column(name = "month")
    private int month;

    @Column(name = "year")
    private int year;
}
