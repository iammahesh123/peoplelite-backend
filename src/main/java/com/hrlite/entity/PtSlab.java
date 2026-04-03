package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Professional Tax slabs by state.
 * Different Indian states have different PT slab structures.
 */
@Entity
@Table(name = "pt_slabs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PtSlab extends TenantAwareEntity {

    @Column(name = "state_name", nullable = false)
    private String stateName;

    @Column(name = "slab_from", precision = 12, scale = 2, nullable = false)
    private BigDecimal slabFrom;

    @Column(name = "slab_to", precision = 12, scale = 2)
    private BigDecimal slabTo;

    @Column(name = "tax_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "order_index")
    @Builder.Default
    private int orderIndex = 0;
}
