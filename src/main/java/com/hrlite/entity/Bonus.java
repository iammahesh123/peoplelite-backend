package com.hrlite.entity;

import com.hrlite.enums.BonusType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bonuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bonus extends TenantAwareEntity {

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "bonus_type", nullable = false)
    private BonusType bonusType;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "apply_to_all")
    @Builder.Default
    private boolean applyToAll = false;

    @Column(name = "created_by")
    private UUID createdBy;
}
