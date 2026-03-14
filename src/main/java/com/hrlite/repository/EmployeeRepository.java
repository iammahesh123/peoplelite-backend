package com.hrlite.repository;

import com.hrlite.entity.Employee;
import com.hrlite.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findByTenantIdAndStatus(UUID tenantId, EmployeeStatus status);

    List<Employee> findByTenantId(UUID tenantId);

    Optional<Employee> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, EmployeeStatus status);
}
