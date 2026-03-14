package com.hrlite.repository;

import com.hrlite.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByTenantIdAndActiveTrue(UUID tenantId);
    List<Department> findByTenantId(UUID tenantId);
    Optional<Department> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Department> findByTenantIdAndName(UUID tenantId, String name);
    Optional<Department> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndName(UUID tenantId, String name);
}
