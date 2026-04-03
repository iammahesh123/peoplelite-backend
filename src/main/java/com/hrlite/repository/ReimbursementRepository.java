package com.hrlite.repository;

import com.hrlite.entity.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, UUID> {
    List<Reimbursement> findByTenantIdAndEmployeeIdAndMonthAndYearAndStatus(
            UUID tenantId, UUID employeeId, int month, int year, String status);
    List<Reimbursement> findByTenantIdAndMonthAndYear(UUID tenantId, int month, int year);
    List<Reimbursement> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
