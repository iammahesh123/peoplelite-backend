package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tracks salary advances and loans given to employees.
 * Supports recurring monthly EMI deductions until fully repaid.
 */
@Entity
@Table(name = "employee_loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLoan extends TenantAwareEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @Column(name = "principal_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "emi_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal emiAmount;

    @Column(name = "total_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 12, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "start_month", nullable = false)
    private int startMonth;

    @Column(name = "start_year", nullable = false)
    private int startYear;

    @Column(name = "disbursal_date")
    private LocalDate disbursalDate;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_by")
    private UUID createdBy;

    @PrePersist
    public void initRemaining() {
        super.prePersist();
        if (this.remainingAmount == null) {
            this.remainingAmount = this.principalAmount;
        }
    }
}
