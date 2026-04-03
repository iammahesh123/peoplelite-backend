package com.hrlite.service;

import com.hrlite.entity.AttendanceRecord;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.AttendanceRecordRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRecordRepository repository;

    @Transactional
    public AttendanceRecord checkIn(UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UUID employeeId = principal.getEmployeeId();
        LocalDate today = LocalDate.now();

        repository.findByTenantIdAndEmployeeIdAndDate(tenantId, employeeId, today)
                .ifPresent(existing -> {
                    if (existing.getCheckInTime() != null) {
                        throw new BusinessException(ErrorCodes.ATTENDANCE_ALREADY_CHECKED_IN,
                                "Already checked in today");
                    }
                });

        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .date(today)
                .checkInTime(LocalDateTime.now())
                .month(today.getMonthValue())
                .year(today.getYear())
                .status("PRESENT")
                .build();

        return repository.save(record);
    }

    @Transactional
    public AttendanceRecord checkOut(UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UUID employeeId = principal.getEmployeeId();
        LocalDate today = LocalDate.now();

        AttendanceRecord record = repository.findByTenantIdAndEmployeeIdAndDate(tenantId, employeeId, today)
                .orElseThrow(() -> new BusinessException(ErrorCodes.ATTENDANCE_NOT_CHECKED_IN,
                        "Not checked in today"));

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutTime(now);

        // Calculate total hours
        if (record.getCheckInTime() != null) {
            long minutes = java.time.Duration.between(record.getCheckInTime(), now).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            record.setTotalHours(hours);
        }

        return repository.save(record);
    }

    @Transactional(readOnly = true)
    public AttendanceRecord getTodayRecord(UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndEmployeeIdAndDate(tenantId, principal.getEmployeeId(), LocalDate.now())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getMyRecords(UserPrincipal principal, int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndEmployeeIdAndMonthAndYear(tenantId, principal.getEmployeeId(), month, year);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAllRecords(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndMonthAndYear(tenantId, month, year);
    }
}
