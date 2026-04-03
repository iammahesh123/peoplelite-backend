package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "overtime_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeLog extends TenantAwareEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "hours", precision = 5, scale = 2, nullable = false)
    private BigDecimal hours;

    @Column(name = "reason")
    private String reason;

    @Column(name = "approved")
    @Builder.Default
    private boolean approved = false;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;
}
