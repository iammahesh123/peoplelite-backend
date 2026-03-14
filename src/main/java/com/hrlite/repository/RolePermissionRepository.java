package com.hrlite.repository;

import com.hrlite.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findByTenantIdAndRole(UUID tenantId, String role);
    List<RolePermission> findByTenantId(UUID tenantId);
}
