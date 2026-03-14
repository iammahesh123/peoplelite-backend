package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "letter_generation_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LetterGenerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "letter_type", nullable = false)
    private String letterType;

    @Column(name = "template_style")
    private String templateStyle;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "emailed_to")
    private String emailedTo;

    @Column(name = "emailed_at")
    private LocalDateTime emailedAt;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }
}
