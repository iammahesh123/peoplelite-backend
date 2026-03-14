package com.hrlite.entity;

import com.hrlite.enums.SeparationStatus;
import com.hrlite.enums.SeparationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "separations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Separation extends TenantAwareEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "separation_type", nullable = false)
    private SeparationType separationType;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "last_working_date")
    private LocalDate lastWorkingDate;

    @Column(name = "notice_period_days")
    @Builder.Default
    private Integer noticePeriodDays = 30;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "exit_interview_notes", columnDefinition = "TEXT")
    private String exitInterviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SeparationStatus status = SeparationStatus.INITIATED;

    @Column(name = "final_settlement_amount", precision = 12, scale = 2)
    private BigDecimal finalSettlementAmount;

    @Column(name = "remaining_leaves", precision = 5, scale = 1)
    private BigDecimal remainingLeaves;
}
