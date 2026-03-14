package com.hrlite.service;

import com.hrlite.dtos.AlertItem;
import com.hrlite.dtos.DashboardStats;
import com.hrlite.dtos.EmployeeDashboardResponse;
import com.hrlite.dtos.FounderDashboardResponse;
import com.hrlite.entity.LeaveBalance;
import com.hrlite.entity.LeaveRequest;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.enums.LeaveStatus;
import com.hrlite.entity.LeaveType;
import com.hrlite.repository.LeaveBalanceRepository;
import com.hrlite.repository.LeaveRequestRepository;
import com.hrlite.repository.LeaveTypeRepository;
import com.hrlite.dtos.LeaveBalanceResponse;
import com.hrlite.dtos.LeaveRequestResponse;
import com.hrlite.entity.OnboardingTask;
import com.hrlite.repository.OnboardingTaskRepository;
import com.hrlite.dtos.OnboardingTaskResponse;
import com.hrlite.entity.LetterGenerationLog;
import com.hrlite.entity.PayrollRun;
import com.hrlite.entity.SelfBoardingInvitation;
import com.hrlite.repository.LetterGenerationLogRepository;
import com.hrlite.repository.PayrollRunRepository;
import com.hrlite.repository.PayslipRepository;
import com.hrlite.repository.SelfBoardingInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;
    private final OnboardingTaskRepository onboardingTaskRepository;
    private final SelfBoardingInvitationRepository selfBoardingInvitationRepository;
    private final LetterGenerationLogRepository letterGenerationLogRepository;

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        UUID tenantId = TenantContext.getCurrentTenant();

        long totalEmployees = employeeRepository.countByTenantId(tenantId);
        long activeEmployees = employeeRepository.countByTenantIdAndStatus(tenantId, EmployeeStatus.ACTIVE);
        long pendingLeaves = leaveRequestRepository.countByTenantIdAndStatus(tenantId, LeaveStatus.PENDING);
        long pendingOnboarding = onboardingTaskRepository.findByTenantIdAndCompletedFalse(tenantId).size();

        // Last payroll info
        String lastPayrollMonth = "N/A";
        BigDecimal lastPayrollTotal = BigDecimal.ZERO;
        List<PayrollRun> runs = payrollRunRepository.findByTenantIdOrderByYearDescMonthDesc(tenantId);
        if (!runs.isEmpty()) {
            PayrollRun latest = runs.get(0);
            lastPayrollMonth = Month.of(latest.getMonth()).name() + " " + latest.getYear();
            lastPayrollTotal = latest.getTotalNet();
        }

        return DashboardStats.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .pendingLeaveRequests(pendingLeaves)
                .lastPayrollMonth(lastPayrollMonth)
                .lastPayrollTotal(lastPayrollTotal)
                .pendingOnboardingTasks(pendingOnboarding)
                .build();
    }

    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getEmployeeDashboard(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Get leave balances
        int currentYear = LocalDate.now().getYear();
        List<LeaveBalanceResponse> leaveBalances = leaveBalanceRepository
                .findByEmployeeIdAndYear(employeeId, currentYear)
                .stream()
                .map(this::mapToLeaveBalanceResponse)
                .collect(Collectors.toList());

        // Calculate total and this month leaves
        double totalLeaveBalance = leaveBalances.stream()
                .mapToDouble(LeaveBalanceResponse::getRemaining)
                .sum();

        double leavesThisMonth = leaveRequestRepository
                .findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .filter(lr -> lr.getStartDate().getMonthValue() == LocalDate.now().getMonthValue()
                        && lr.getStartDate().getYear() == LocalDate.now().getYear())
                .mapToDouble(lr -> lr.getDays())
                .sum();

        // Get recent leaves (last 5)
        List<LeaveRequestResponse> recentLeaves = leaveRequestRepository
                .findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .limit(5)
                .map(this::mapToLeaveRequestResponse)
                .collect(Collectors.toList());

        // Get recent payslips (last 3)
        List<EmployeeDashboardResponse.RecentPayslip> recentPayslips = payslipRepository
                .findByEmployeeIdOrderByYearDescMonthDesc(employeeId)
                .stream()
                .limit(3)
                .map(payslip -> EmployeeDashboardResponse.RecentPayslip.builder()
                        .id(payslip.getId().toString())
                        .month(payslip.getMonth())
                        .year(payslip.getYear())
                        .netPay(payslip.getNetPay())
                        .status("GENERATED")
                        .build())
                .collect(Collectors.toList());

        // Get payroll info for next payroll date
        String nextPayrollDate = "TBD";
        String latestPayslipStatus = "N/A";
        String latestPayslipMonth = "N/A";

        List<PayrollRun> payrollRuns = payrollRunRepository.findByTenantIdOrderByYearDescMonthDesc(tenantId);
        if (!payrollRuns.isEmpty()) {
            PayrollRun latest = payrollRuns.get(0);
            latestPayslipStatus = latest.getStatus().toString();
            latestPayslipMonth = Month.of(latest.getMonth()).name() + " " + latest.getYear();
        }

        // Get onboarding tasks
        List<OnboardingTask> allTasks = onboardingTaskRepository.findByEmployeeIdOrderByOrderIndexAsc(employeeId);
        List<OnboardingTask> pendingTasks = allTasks.stream()
                .filter(t -> !t.isCompleted())
                .limit(5)
                .collect(Collectors.toList());

        long completedCount = allTasks.stream().filter(OnboardingTask::isCompleted).count();

        EmployeeDashboardResponse.OnboardingInfo onboardingInfo = EmployeeDashboardResponse.OnboardingInfo.builder()
                .totalTasks(allTasks.size())
                .completedTasks((int) completedCount)
                .pendingTasks(pendingTasks.stream()
                        .map(this::mapToOnboardingTaskResponse)
                        .collect(Collectors.toList()))
                .build();

        EmployeeDashboardResponse.QuickInfo quickInfo = EmployeeDashboardResponse.QuickInfo.builder()
                .totalLeaveBalance(totalLeaveBalance)
                .leavesThisMonth(leavesThisMonth)
                .nextPayrollDate(nextPayrollDate)
                .latestPayslipStatus(latestPayslipStatus)
                .latestPayslipMonth(latestPayslipMonth)
                .build();

        return EmployeeDashboardResponse.builder()
                .quickInfo(quickInfo)
                .leaveBalances(leaveBalances)
                .recentLeaves(recentLeaves)
                .recentPayslips(recentPayslips)
                .onboardingTasks(onboardingInfo)
                .build();
    }

    @Transactional(readOnly = true)
    public FounderDashboardResponse getFounderDashboard() {
        UUID tenantId = TenantContext.getCurrentTenant();

        // KPIs
        long totalEmployees = employeeRepository.countByTenantId(tenantId);
        long activeEmployees = employeeRepository.countByTenantIdAndStatus(tenantId, EmployeeStatus.ACTIVE);
        long pendingLeaves = leaveRequestRepository.countByTenantIdAndStatus(tenantId, LeaveStatus.PENDING);

        List<PayrollRun> payrollRuns = payrollRunRepository.findByTenantIdOrderByYearDescMonthDesc(tenantId);
        String payrollStatus = payrollRuns.isEmpty() ? "NO_RUNS" : payrollRuns.get(0).getStatus().toString();

        // Upcoming joiners (next 30 days)
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        long upcomingJoinees = employeeRepository.findByTenantId(tenantId).stream()
                .filter(e -> e.getDateOfJoining().isAfter(today) && e.getDateOfJoining().isBefore(futureDate))
                .count();

        FounderDashboardResponse.KPIs kpis = FounderDashboardResponse.KPIs.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .pendingLeaveRequests(pendingLeaves)
                .payrollStatus(payrollStatus)
                .upcomingJoinees(upcomingJoinees)
                .build();

        // Leave Overview
        List<FounderDashboardResponse.OnLeaveItem> onLeaveToday = new ArrayList<>();
        List<FounderDashboardResponse.UpcomingLeave> upcomingLeaves = new ArrayList<>();
        List<LeaveRequestResponse> pendingApprovals = leaveRequestRepository
                .findByTenantIdAndStatus(tenantId, LeaveStatus.PENDING)
                .stream()
                .limit(10)
                .map(this::mapToLeaveRequestResponse)
                .collect(Collectors.toList());

        FounderDashboardResponse.LeaveOverview leaveOverview = FounderDashboardResponse.LeaveOverview.builder()
                .onLeaveToday(onLeaveToday)
                .upcomingLeaves(upcomingLeaves)
                .pendingApprovals(pendingApprovals)
                .build();

        // Payroll Overview
        PayrollRun currentPayroll = payrollRuns.isEmpty() ? null : payrollRuns.get(0);
        FounderDashboardResponse.PayrollOverview payrollOverview = FounderDashboardResponse.PayrollOverview.builder()
                .currentCycleStatus(payrollStatus)
                .month(currentPayroll != null ? currentPayroll.getMonth() : LocalDate.now().getMonthValue())
                .year(currentPayroll != null ? currentPayroll.getYear() : LocalDate.now().getYear())
                .employeesProcessed(currentPayroll != null ? currentPayroll.getEmployeeCount() : 0)
                .totalEmployees(activeEmployees)
                .totalPayout(currentPayroll != null ? currentPayroll.getTotalNet() : BigDecimal.ZERO)
                .currency("INR")
                .pendingGeneration(currentPayroll == null || "DRAFT".equals(currentPayroll.getStatus().toString()))
                .build();

        // Onboarding Tracker
        List<FounderDashboardResponse.NewEmployee> newEmployees = employeeRepository.findByTenantId(tenantId)
                .stream()
                .filter(e -> e.getDateOfJoining().isAfter(LocalDate.now().minusDays(90)))
                .limit(5)
                .map(e -> {
                    List<OnboardingTask> tasks = onboardingTaskRepository.findByEmployeeIdOrderByOrderIndexAsc(e.getId());
                    long completed = tasks.stream().filter(OnboardingTask::isCompleted).count();
                    return FounderDashboardResponse.NewEmployee.builder()
                            .employeeId(e.getId().toString())
                            .employeeName(e.getFullName())
                            .dateOfJoining(e.getDateOfJoining())
                            .progress((int) ((completed * 100) / Math.max(tasks.size(), 1)))
                            .totalTasks(tasks.size())
                            .completedTasks((int) completed)
                            .build();
                })
                .collect(Collectors.toList());

        FounderDashboardResponse.OnboardingTracker onboardingTracker = FounderDashboardResponse.OnboardingTracker.builder()
                .newEmployees(newEmployees)
                .pendingOfferLetters(0)
                .missingDocuments(0)
                .build();

        // Alerts (mock data)
        List<AlertItem> alerts = new ArrayList<>();
        if (pendingLeaves > 0) {
            alerts.add(AlertItem.builder()
                    .id(UUID.randomUUID().toString())
                    .type("WARNING")
                    .title("Pending Leave Approvals")
                    .message("You have " + pendingLeaves + " pending leave requests")
                    .createdAt(LocalDateTime.now())
                    .read(false)
                    .build());
        }

        // Self-Boarding Overview
        List<SelfBoardingInvitation> allInvitations = selfBoardingInvitationRepository.findByTenantIdOrderBySentAtDesc(tenantId);
        long totalInvitations = allInvitations.size();
        long pendingInvitations = allInvitations.stream().filter(i -> "PENDING".equals(i.getStatus())).count();
        long completedInvitations = allInvitations.stream().filter(i -> "COMPLETED".equals(i.getStatus())).count();

        List<FounderDashboardResponse.RecentInvitation> recentInvitations = allInvitations.stream()
                .limit(5)
                .map(inv -> {
                    String empName = employeeRepository.findById(inv.getEmployeeId())
                            .map(e -> e.getFullName()).orElse("Unknown");
                    return FounderDashboardResponse.RecentInvitation.builder()
                            .employeeName(empName)
                            .email(inv.getEmail())
                            .status(inv.getStatus())
                            .sentAt(inv.getSentAt() != null ? inv.getSentAt().toString() : null)
                            .completedAt(inv.getCompletedAt() != null ? inv.getCompletedAt().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());

        FounderDashboardResponse.SelfBoardingOverview selfBoardingOverview = FounderDashboardResponse.SelfBoardingOverview.builder()
                .totalInvitations(totalInvitations)
                .pendingInvitations(pendingInvitations)
                .completedInvitations(completedInvitations)
                .recentInvitations(recentInvitations)
                .build();

        // Letter Activity
        List<LetterGenerationLog> allLogs = letterGenerationLogRepository.findByTenantIdOrderByGeneratedAtDesc(tenantId);
        long totalLetters = allLogs.size();

        List<FounderDashboardResponse.RecentLetter> recentLetters = allLogs.stream()
                .limit(10)
                .map(log -> FounderDashboardResponse.RecentLetter.builder()
                        .employeeName(log.getEmployeeName())
                        .letterType(log.getLetterType())
                        .templateStyle(log.getTemplateStyle())
                        .generatedAt(log.getGeneratedAt() != null ? log.getGeneratedAt().toString() : null)
                        .emailedTo(log.getEmailedTo())
                        .build())
                .collect(Collectors.toList());

        FounderDashboardResponse.LetterActivity letterActivity = FounderDashboardResponse.LetterActivity.builder()
                .totalLettersGenerated(totalLetters)
                .recentLetters(recentLetters)
                .build();

        return FounderDashboardResponse.builder()
                .kpis(kpis)
                .leaveOverview(leaveOverview)
                .payrollOverview(payrollOverview)
                .onboardingTracker(onboardingTracker)
                .alerts(alerts)
                .selfBoardingOverview(selfBoardingOverview)
                .letterActivity(letterActivity)
                .build();
    }

    private LeaveBalanceResponse mapToLeaveBalanceResponse(LeaveBalance lb) {
        LeaveType leaveType = leaveTypeRepository.findById(lb.getLeaveTypeId()).orElse(null);
        String leaveTypeName = leaveType != null ? leaveType.getName() : "Unknown";
        String leaveTypeCode = leaveType != null ? leaveType.getCode() : "UNKNOWN";

        return LeaveBalanceResponse.builder()
                .leaveTypeId(lb.getLeaveTypeId())
                .leaveTypeName(leaveTypeName)
                .leaveTypeCode(leaveTypeCode)
                .total(lb.getTotal())
                .used(lb.getUsed())
                .remaining(lb.getRemaining())
                .year(lb.getYear())
                .build();
    }

    private LeaveRequestResponse mapToLeaveRequestResponse(LeaveRequest lr) {
        return LeaveRequestResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployeeId())
                .leaveTypeId(lr.getLeaveTypeId())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .days(lr.getDays())
                .reason(lr.getReason())
                .status(lr.getStatus().toString())
                .remarks(lr.getRemarks())
                .createdAt(lr.getCreatedAt())
                .approvedAt(lr.getApprovedAt())
                .build();
    }

    private OnboardingTaskResponse mapToOnboardingTaskResponse(OnboardingTask task) {
        return OnboardingTaskResponse.builder()
                .id(task.getId())
                .employeeId(task.getEmployeeId())
                .taskName(task.getTaskName())
                .description(task.getDescription())
                .orderIndex(task.getOrderIndex())
                .completed(task.isCompleted())
                .completedAt(task.getCompletedAt())
                .build();
    }
}
