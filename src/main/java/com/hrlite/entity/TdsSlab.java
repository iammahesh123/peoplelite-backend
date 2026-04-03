package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * TDS Tax Slabs for the New Tax Regime (India).
 * Stores configurable slab ranges and rates per tenant.
 */
@Entity
@Table(name = "tds_slabs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TdsSlab extends TenantAwareEntity {

    @Column(name = "slab_from", precision = 14, scale = 2, nullable = false)
    private BigDecimal slabFrom;

    @Column(name = "slab_to", precision = 14, scale = 2)
    private BigDecimal slabTo;

    @Column(name = "rate_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal ratePercent;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "regime")
    @Builder.Default
    private String regime = "NEW";
}
