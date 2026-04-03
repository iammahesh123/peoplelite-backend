package com.hrlite.repository;

import com.hrlite.entity.OvertimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OvertimeLogRepository extends JpaRepository<OvertimeLog, UUID> {

    List<OvertimeLog> findByTenantIdAndMonthAndYear(UUID tenantId, int month, int year);

    List<OvertimeLog> findByTenantIdAndEmployeeIdAndMonthAndYear(UUID tenantId, UUID employeeId, int month, int year);

    List<OvertimeLog> findByTenantIdAndEmployeeIdAndMonthAndYearAndApproved(
            UUID tenantId, UUID employeeId, int month, int year, boolean approved);

    List<OvertimeLog> findByTenantIdOrderByDateDesc(UUID tenantId);

    List<OvertimeLog> findByEmployeeIdOrderByDateDesc(UUID employeeId);
}
