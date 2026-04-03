package com.hrlite.service;

import com.hrlite.entity.OvertimeLog;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.repository.OvertimeLogRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeService {

    private final OvertimeLogRepository repository;

    @Transactional
    public OvertimeLog logOvertime(UUID employeeId, LocalDate date, BigDecimal hours, String reason, UserPrincipal principal) {
        OvertimeLog ot = OvertimeLog.builder()
                .employeeId(employeeId)
                .date(date)
                .hours(hours)
                .reason(reason)
                .month(date.getMonthValue())
                .year(date.getYear())
                .approved(false)
                .build();
        return repository.save(ot);
    }

    @Transactional
    public OvertimeLog approveOvertime(UUID overtimeId, UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        OvertimeLog ot = repository.findById(overtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimeLog", overtimeId));
        ot.setApproved(true);
        ot.setApprovedBy(principal.getUserId());
        return repository.save(ot);
    }

    @Transactional(readOnly = true)
    public List<OvertimeLog> getOvertimeLogs(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndMonthAndYear(tenantId, month, year);
    }

    @Transactional(readOnly = true)
    public List<OvertimeLog> getMyOvertimeLogs(UUID employeeId) {
        return repository.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<OvertimeLog> getAllOvertimeLogs() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdOrderByDateDesc(tenantId);
    }
}
