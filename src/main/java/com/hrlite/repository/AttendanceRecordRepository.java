package com.hrlite.repository;

import com.hrlite.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByTenantIdAndEmployeeIdAndDate(UUID tenantId, UUID employeeId, LocalDate date);

    List<AttendanceRecord> findByTenantIdAndEmployeeIdAndMonthAndYear(UUID tenantId, UUID employeeId, int month, int year);

    List<AttendanceRecord> findByTenantIdAndMonthAndYear(UUID tenantId, int month, int year);

    List<AttendanceRecord> findByTenantIdAndEmployeeIdOrderByDateDesc(UUID tenantId, UUID employeeId);

    long countByTenantIdAndEmployeeIdAndMonthAndYear(UUID tenantId, UUID employeeId, int month, int year);
}
