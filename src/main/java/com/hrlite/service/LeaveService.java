package com.hrlite.service;

import com.hrlite.enums.LeaveStatus;
import com.hrlite.dtos.*;
import com.hrlite.entity.*;
import com.hrlite.repository.*;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationEventService notificationEventService;
    private final UserRepository userRepository;

    // --- Leave Type Management ---

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getLeaveTypes() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return leaveTypeRepository.findByTenantId(tenantId).stream()
                .map(this::toLeaveTypeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveTypeResponse createLeaveType(LeaveTypeRequest request) {
        LeaveType leaveType = LeaveType.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .defaultBalance(request.getDefaultBalance())
                .paid(request.isPaid())
                .build();
        leaveType = leaveTypeRepository.save(leaveType);

        // Auto-create balances for all existing active employees
        UUID tenantId = TenantContext.getCurrentTenant();
        int year = LocalDate.now().getYear();
        List<Employee> employees = employeeRepository.findByTenantId(tenantId);
        for (Employee emp : employees) {
            Optional<LeaveBalance> existing = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(emp.getId(), leaveType.getId(), year);
            if (existing.isEmpty()) {
                LeaveBalance balance = LeaveBalance.builder()
                        .employeeId(emp.getId())
                        .leaveTypeId(leaveType.getId())
                        .year(year)
                        .total(leaveType.getDefaultBalance())
                        .build();
                leaveBalanceRepository.save(balance);
            }
        }

        return toLeaveTypeResponse(leaveType);
    }

    @Transactional
    public LeaveTypeResponse updateLeaveType(UUID id, LeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", id));

        int oldDefaultBalance = leaveType.getDefaultBalance();
        leaveType.setName(request.getName());
        leaveType.setCode(request.getCode().toUpperCase());
        leaveType.setDefaultBalance(request.getDefaultBalance());
        leaveType.setPaid(request.isPaid());
        leaveType = leaveTypeRepository.save(leaveType);

        // If default balance changed, update all employee balances for current year
        if (oldDefaultBalance != request.getDefaultBalance()) {
            int year = LocalDate.now().getYear();
            List<LeaveBalance> balances = leaveBalanceRepository.findByLeaveTypeIdAndYear(leaveType.getId(), year);
            for (LeaveBalance balance : balances) {
                // Adjust total: add the difference (handles both increase and decrease)
                double diff = request.getDefaultBalance() - oldDefaultBalance;
                double newTotal = Math.max(balance.getUsed(), balance.getTotal() + diff);
                balance.setTotal(newTotal);
                leaveBalanceRepository.save(balance);
            }
        }

        return toLeaveTypeResponse(leaveType);
    }

    @Transactional
    public void deleteLeaveType(UUID id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", id));
        leaveType.setActive(false);
        leaveTypeRepository.save(leaveType);
    }

    // --- Leave Balance ---

    @Transactional
    public void initializeBalancesForEmployee(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        int year = LocalDate.now().getYear();
        List<LeaveType> leaveTypes = leaveTypeRepository.findByTenantIdAndActiveTrue(tenantId);

        for (LeaveType lt : leaveTypes) {
            Optional<LeaveBalance> existing = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, lt.getId(), year);
            if (existing.isEmpty()) {
                LeaveBalance balance = LeaveBalance.builder()
                        .employeeId(employeeId)
                        .leaveTypeId(lt.getId())
                        .year(year)
                        .total(lt.getDefaultBalance())
                        .build();
                leaveBalanceRepository.save(balance);
            }
        }
    }

    @Transactional
    public List<LeaveBalanceResponse> getMyBalances(UUID employeeId) {
        int year = LocalDate.now().getYear();
        UUID tenantId = TenantContext.getCurrentTenant();

        // Get all active leave types for this tenant
        List<LeaveType> activeTypes = leaveTypeRepository.findByTenantIdAndActiveTrue(tenantId);
        Map<UUID, LeaveType> typeMap = activeTypes.stream()
                .collect(Collectors.toMap(lt -> lt.getId(), lt -> lt));

        // Get existing balances
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
        Set<UUID> existingTypeIds = balances.stream()
                .map(LeaveBalance::getLeaveTypeId)
                .collect(Collectors.toSet());

        // Auto-create missing balances for any new leave types
        for (LeaveType lt : activeTypes) {
            if (!existingTypeIds.contains(lt.getId())) {
                LeaveBalance newBalance = LeaveBalance.builder()
                        .employeeId(employeeId)
                        .leaveTypeId(lt.getId())
                        .year(year)
                        .total(lt.getDefaultBalance())
                        .build();
                balances.add(leaveBalanceRepository.save(newBalance));
            }
        }

        return balances.stream()
                .filter(b -> typeMap.containsKey(b.getLeaveTypeId())) // only active types
                .map(b -> {
                    LeaveType lt = typeMap.get(b.getLeaveTypeId());
                    return LeaveBalanceResponse.builder()
                            .leaveTypeId(b.getLeaveTypeId())
                            .leaveTypeName(lt.getName())
                            .leaveTypeCode(lt.getCode())
                            .total(b.getTotal())
                            .used(b.getUsed())
                            .remaining(b.getRemaining())
                            .year(b.getYear())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Allows founder to manually adjust an employee's leave balance for a specific leave type.
     */
    @Transactional
    public LeaveBalanceResponse adjustBalance(UUID employeeId, UUID leaveTypeId, double newTotal) {
        int year = LocalDate.now().getYear();
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", leaveTypeId));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseGet(() -> {
                    LeaveBalance nb = LeaveBalance.builder()
                            .employeeId(employeeId)
                            .leaveTypeId(leaveTypeId)
                            .year(year)
                            .total(0)
                            .build();
                    return leaveBalanceRepository.save(nb);
                });

        // Ensure new total is not less than already used
        double adjustedTotal = Math.max(balance.getUsed(), newTotal);
        balance.setTotal(adjustedTotal);
        balance = leaveBalanceRepository.save(balance);

        return LeaveBalanceResponse.builder()
                .leaveTypeId(balance.getLeaveTypeId())
                .leaveTypeName(leaveType.getName())
                .leaveTypeCode(leaveType.getCode())
                .total(balance.getTotal())
                .used(balance.getUsed())
                .remaining(balance.getRemaining())
                .year(balance.getYear())
                .build();
    }

    /**
     * Get all employee balances for a specific employee (founder view).
     */
    @Transactional
    public List<LeaveBalanceResponse> getEmployeeBalances(UUID employeeId) {
        return getMyBalances(employeeId);
    }

    // --- Leave Requests ---

    @Transactional
    public LeaveRequestResponse applyLeave(UUID employeeId, ApplyLeaveRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(ErrorCodes.LEAVE_INVALID_DATES, "End date must be after start date");
        }

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", request.getLeaveTypeId()));

        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Check balance with pessimistic lock
        int year = request.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearForUpdate(employeeId, leaveType.getId(), year)
                .orElseThrow(() -> new BusinessException(ErrorCodes.LEAVE_INSUFFICIENT_BALANCE,
                        "No leave balance found. Please contact your admin."));

        if (balance.getRemaining() < days) {
            throw new BusinessException(ErrorCodes.LEAVE_INSUFFICIENT_BALANCE,
                    "Insufficient leave balance. Available: " + balance.getRemaining() + ", Requested: " + days);
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveType.getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(days)
                .reason(request.getReason())
                .build();

        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // Trigger leave applied notification event
        UUID tenantId = TenantContext.getCurrentTenant();
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        String employeeName = employee != null ? employee.getFullName() : "Employee";
        notificationEventService.onLeaveApplied(tenantId, employeeId, employeeName, leaveType.getName(),
                request.getStartDate().toString(), request.getEndDate().toString(), request.getReason());

        return toLeaveRequestResponse(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAllLeaveRequests() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return leaveRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toLeaveRequestResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getMyLeaveRequests(UUID employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toLeaveRequestResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponse approveLeave(UUID leaveId, UserPrincipal approver, LeaveActionRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", leaveId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(ErrorCodes.LEAVE_ALREADY_PROCESSED, "Leave request already processed");
        }

        // Deduct balance
        int year = leaveRequest.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearForUpdate(
                        leaveRequest.getEmployeeId(), leaveRequest.getLeaveTypeId(), year)
                .orElseThrow(() -> new BusinessException(ErrorCodes.LEAVE_INSUFFICIENT_BALANCE, "Leave balance not found"));

        if (balance.getRemaining() < leaveRequest.getDays()) {
            throw new BusinessException(ErrorCodes.LEAVE_INSUFFICIENT_BALANCE, "Insufficient leave balance");
        }

        balance.setUsed(balance.getUsed() + leaveRequest.getDays());
        leaveBalanceRepository.save(balance);

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver.getUserId());
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setRemarks(request != null ? request.getRemarks() : null);
        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // Trigger leave approved notification event
        UUID tenantId = TenantContext.getCurrentTenant();
        Employee employee = employeeRepository.findById(leaveRequest.getEmployeeId()).orElse(null);
        String employeeName = employee != null ? employee.getFullName() : "Employee";
        LeaveType leaveTypeEntity = leaveTypeRepository.findById(leaveRequest.getLeaveTypeId()).orElse(null);
        String leaveTypeName = leaveTypeEntity != null ? leaveTypeEntity.getName() : "Leave";

        // Get the employee's user ID
        User employeeUser = userRepository.findByEmployeeId(leaveRequest.getEmployeeId()).orElse(null);
        if (employeeUser != null) {
            notificationEventService.onLeaveApproved(tenantId, employeeUser.getId(), employeeName, leaveTypeName,
                    leaveRequest.getStartDate().toString(), leaveRequest.getEndDate().toString(),
                    leaveRequest.getRemarks());
        }

        return toLeaveRequestResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse rejectLeave(UUID leaveId, UserPrincipal approver, LeaveActionRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", leaveId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(ErrorCodes.LEAVE_ALREADY_PROCESSED, "Leave request already processed");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver.getUserId());
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setRemarks(request != null ? request.getRemarks() : null);
        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // Trigger leave rejected notification event
        UUID tenantId = TenantContext.getCurrentTenant();
        Employee employee = employeeRepository.findById(leaveRequest.getEmployeeId()).orElse(null);
        String employeeName = employee != null ? employee.getFullName() : "Employee";
        LeaveType leaveTypeEntity = leaveTypeRepository.findById(leaveRequest.getLeaveTypeId()).orElse(null);
        String leaveTypeName = leaveTypeEntity != null ? leaveTypeEntity.getName() : "Leave";

        // Get the employee's user ID
        User employeeUser = userRepository.findByEmployeeId(leaveRequest.getEmployeeId()).orElse(null);
        if (employeeUser != null) {
            notificationEventService.onLeaveRejected(tenantId, employeeUser.getId(), employeeName, leaveTypeName,
                    leaveRequest.getStartDate().toString(), leaveRequest.getEndDate().toString(),
                    leaveRequest.getRemarks());
        }

        return toLeaveRequestResponse(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getLeaveCalendar(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return leaveRequestRepository.findApprovedLeavesInRange(tenantId, startDate, endDate).stream()
                .map(this::toLeaveRequestResponse)
                .collect(Collectors.toList());
    }

    private LeaveTypeResponse toLeaveTypeResponse(LeaveType lt) {
        return LeaveTypeResponse.builder()
                .id(lt.getId())
                .name(lt.getName())
                .code(lt.getCode())
                .defaultBalance(lt.getDefaultBalance())
                .paid(lt.isPaid())
                .active(lt.isActive())
                .build();
    }

    private LeaveRequestResponse toLeaveRequestResponse(LeaveRequest lr) {
        String employeeName = "";
        Employee emp = employeeRepository.findById(lr.getEmployeeId()).orElse(null);
        if (emp != null) {
            employeeName = emp.getFullName();
        }

        String leaveTypeName = "";
        LeaveType lt = leaveTypeRepository.findById(lr.getLeaveTypeId()).orElse(null);
        if (lt != null) {
            leaveTypeName = lt.getName();
        }

        return LeaveRequestResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployeeId())
                .employeeName(employeeName)
                .leaveTypeId(lr.getLeaveTypeId())
                .leaveTypeName(leaveTypeName)
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .days(lr.getDays())
                .reason(lr.getReason())
                .status(lr.getStatus().name())
                .remarks(lr.getRemarks())
                .createdAt(lr.getCreatedAt())
                .approvedAt(lr.getApprovedAt())
                .build();
    }
}
