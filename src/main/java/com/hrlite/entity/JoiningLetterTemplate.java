package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "joining_letter_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoiningLetterTemplate extends TenantAwareEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "html_content", columnDefinition = "TEXT", nullable = false)
    private String htmlContent;

    @Column(name = "variables_json", columnDefinition = "TEXT")
    private String variablesJson;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false)
    @Builder.Default
    private TemplateStyle style = TemplateStyle.CORPORATE;
}
