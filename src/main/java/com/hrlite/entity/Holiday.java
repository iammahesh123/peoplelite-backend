package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Company holidays configured per tenant.
 * Used to auto-calculate working days (excluding weekends + holidays).
 */
@Entity
@Table(name = "holidays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends TenantAwareEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "is_optional")
    @Builder.Default
    private boolean optional = false;
}
