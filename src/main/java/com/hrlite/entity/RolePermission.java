package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "role_permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "permission", nullable = false)
    private String permission;

    @Column(name = "granted", nullable = false)
    @Builder.Default
    private boolean granted = true;
}
