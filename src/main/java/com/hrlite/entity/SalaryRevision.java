package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "salary_revisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryRevision extends TenantAwareEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "previous_ctc", precision = 12, scale = 2)
    private BigDecimal previousCTC;

    @Column(name = "new_ctc", nullable = false, precision = 12, scale = 2)
    private BigDecimal newCTC;

    @Column(name = "previous_basic", precision = 12, scale = 2)
    private BigDecimal previousBasic;

    @Column(name = "new_basic", precision = 12, scale = 2)
    private BigDecimal newBasic;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "revised_by")
    private UUID revisedBy;
}
