package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "salary_slip_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySlipTemplate extends TenantAwareEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "html_content", columnDefinition = "TEXT", nullable = false)
    private String htmlContent;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false)
    @Builder.Default
    private TemplateStyle style = TemplateStyle.CORPORATE;
}
