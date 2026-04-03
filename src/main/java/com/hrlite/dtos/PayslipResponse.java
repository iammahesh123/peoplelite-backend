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
public class PayslipResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private BigDecimal grossEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private BigDecimal basic;
    private BigDecimal hra;
    private BigDecimal specialAllowance;
    private BigDecimal pfEmployee;
    private BigDecimal professionalTax;
    private BigDecimal otherDeductions;
    private BigDecimal esiEmployee;
    private BigDecimal esiEmployer;
    private BigDecimal pfEmployer;
    private BigDecimal tds;
    private String deductionRemarks;
    private int workingDays;
    private double lopDays;
    private BigDecimal overtimeHours;
    private BigDecimal overtimePay;
    private BigDecimal totalBonus;
    private String bonusDetails;
    private BigDecimal lopDeduction;
    private int daysPresent;
    private BigDecimal loanDeduction;
    private String loanDetails;
    private BigDecimal totalReimbursement;
    private String reimbursementDetails;
    private int month;
    private int year;
}
