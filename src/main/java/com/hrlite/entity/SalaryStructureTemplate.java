package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Salary structure template that auto-splits CTC into components.
 * e.g. "Standard" template: 50% Basic, 25% HRA, 25% Special Allowance.
 */
@Entity
@Table(name = "salary_structure_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructureTemplate extends TenantAwareEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "basic_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal basicPercent = new BigDecimal("50.00");

    @Column(name = "hra_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal hraPercent = new BigDecimal("25.00");

    @Column(name = "special_allowance_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal specialAllowancePercent = new BigDecimal("25.00");

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}
