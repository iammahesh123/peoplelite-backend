package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Employee reimbursement claims (travel, medical, meal, etc.)
 * that get included in payroll as non-taxable additions.
 */
@Entity
@Table(name = "reimbursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reimbursement extends TenantAwareEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "status")
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "approved_by")
    private UUID approvedBy;
}
