package com.hrlite.repository;

import com.hrlite.entity.LeaveRequest;
import com.hrlite.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    List<LeaveRequest> findByTenantIdAndStatus(UUID tenantId, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.tenantId = :tenantId AND lr.status = 'APPROVED' " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findApprovedLeavesInRange(UUID tenantId, LocalDate startDate, LocalDate endDate);

    long countByTenantIdAndStatus(UUID tenantId, LeaveStatus status);
}
