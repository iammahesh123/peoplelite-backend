package com.hrlite.repository;

import com.hrlite.entity.EmployeeLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, UUID> {
    List<EmployeeLoan> findByTenantIdAndEmployeeIdAndActiveTrue(UUID tenantId, UUID employeeId);
    List<EmployeeLoan> findByTenantIdAndActiveTrue(UUID tenantId);
    List<EmployeeLoan> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
