package com.hrlite.entity;

import com.hrlite.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "domain")
    private String domain;

    @Column(name = "plan", nullable = false)
    @Builder.Default
    private String plan = "FREE";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "employee_code_prefix")
    @Builder.Default
    private String employeeCodePrefix = "EMP";

    @Column(name = "next_employee_number")
    @Builder.Default
    private int nextEmployeeNumber = 1;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "ceo_signature_url")
    private String ceoSignatureUrl;

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_phone")
    private String companyPhone;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "company_cin")
    private String companyCin;

    @Column(name = "ceo_name")
    private String ceoName;

    public String generateNextEmployeeCode() {
        String code = employeeCodePrefix + String.format("%04d", nextEmployeeNumber);
        nextEmployeeNumber++;
        return code;
    }
}
