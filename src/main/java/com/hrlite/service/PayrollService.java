package com.hrlite.service;

import com.hrlite.dtos.PayrollRunResponse;
import com.hrlite.dtos.PayslipResponse;
import com.hrlite.dtos.RunPayrollRequest;
import com.hrlite.entity.*;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.enums.PayrollStatus;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.excel.PayrollExcelExporter;
import com.hrlite.repository.*;
import com.hrlite.security.UserPrincipal;
import com.hrlite.utils.PayslipPdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;
    private final PayslipPdfGenerator pdfGenerator;
    private final PayrollExcelExporter excelExporter;
    private final NotificationEventService notificationEventService;
    private final PayrollSettingsService payrollSettingsService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final OvertimeLogRepository overtimeLogRepository;
    private final BonusRepository bonusRepository;

    // ── New service dependencies for Round 2 enhancements ──
    private final TdsCalculationService tdsCalculationService;
    private final EmployeeLoanService employeeLoanService;
    private final ReimbursementService reimbursementService;
    private final PtSlabService ptSlabService;
    private final HolidayService holidayService;

    @Transactional
    public PayrollRunResponse generatePayroll(RunPayrollRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Check if payroll already run for this month (allow re-run if previous was REVERSED)
        payrollRunRepository.findByTenantIdAndMonthAndYear(tenantId, request.getMonth(), request.getYear())
                .ifPresent(existing -> {
                    if (existing.getStatus() != PayrollStatus.REVERSED) {
                        throw new BusinessException(ErrorCodes.PAYROLL_ALREADY_RUN,
                                "Payroll already generated for " + request.getMonth() + "/" + request.getYear());
                    }
                });

        // Get active employees
        List<Employee> employees = employeeRepository.findByTenantIdAndStatus(tenantId, EmployeeStatus.ACTIVE);
        if (employees.isEmpty()) {
            throw new BusinessException(ErrorCodes.PAYROLL_NO_EMPLOYEES, "No active employees found");
        }

        // Create payroll run
        PayrollRun run = PayrollRun.builder()
                .month(request.getMonth())
                .year(request.getYear())
                .status(PayrollStatus.PROCESSING)
                .runDate(LocalDate.now())
                .generatedBy(principal.getUserId())
                .employeeCount(employees.size())
                .build();
        run = payrollRunRepository.save(run);

        // Load tenant payroll compliance settings
        PayrollSettings settings = payrollSettingsService.getSettingsEntity(tenantId);

        // Calculate actual working days in the month (weekdays minus mandatory holidays)
        int workingDays = request.getWorkingDays();
        if (workingDays <= 0) {
            workingDays = calculateWorkingDaysInMonth(request.getMonth(), request.getYear());
            // Subtract mandatory holidays that fall on weekdays
            int mandatoryHolidays = holidayService.getMandatoryHolidayCountInMonth(
                    request.getMonth(), request.getYear());
            workingDays = Math.max(1, workingDays - mandatoryHolidays);
        }

        // Pre-fetch leave types to check paid/unpaid
        List<LeaveType> leaveTypes = leaveTypeRepository.findByTenantId(tenantId);

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        List<Payslip> payslips = new ArrayList<>();

        for (Employee emp : employees) {
            BigDecimal basic = emp.getBasicSalary();
            BigDecimal hra = emp.getHra();
            BigDecimal specialAllowance = emp.getSpecialAllowance();

            // ── 1. Calculate LOP (Loss of Pay) from approved unpaid leaves ──
            double lopDays = calculateLopDays(tenantId, emp.getId(), request.getMonth(), request.getYear(), leaveTypes);
            BigDecimal dailyRate = basic.add(hra).add(specialAllowance)
                    .divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);
            BigDecimal lopDeduction = dailyRate.multiply(BigDecimal.valueOf(lopDays))
                    .setScale(2, RoundingMode.HALF_UP);

            // ── 2. Calculate attendance-based days present ──
            int daysPresent = calculateDaysPresent(tenantId, emp.getId(), request.getMonth(), request.getYear());

            // ── 3. Calculate Overtime Pay ──
            BigDecimal overtimeHours = BigDecimal.ZERO;
            BigDecimal overtimePay = BigDecimal.ZERO;
            if (settings.isOvertimeEnabled()) {
                overtimeHours = calculateApprovedOvertimeHours(tenantId, emp.getId(), request.getMonth(), request.getYear());
                BigDecimal hourlyRate = basic.add(hra).add(specialAllowance)
                        .divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP)
                        .divide(settings.getStandardHoursPerDay(), 4, RoundingMode.HALF_UP);
                overtimePay = hourlyRate.multiply(settings.getOvertimeMultiplier())
                        .multiply(overtimeHours)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // ── 4. Calculate Bonuses (festival + ad-hoc) ──
            BigDecimal totalBonus = BigDecimal.ZERO;
            StringBuilder bonusDetails = new StringBuilder();
            List<Bonus> bonuses = bonusRepository.findBonusesForEmployee(tenantId, emp.getId(), request.getMonth(), request.getYear());
            for (Bonus bonus : bonuses) {
                totalBonus = totalBonus.add(bonus.getAmount());
                if (bonusDetails.length() > 0) bonusDetails.append("; ");
                bonusDetails.append(bonus.getName()).append(": ₹").append(bonus.getAmount().toPlainString());
            }

            // ── 5. Calculate Approved Reimbursements ──
            BigDecimal totalReimbursement = BigDecimal.ZERO;
            StringBuilder reimbursementDetails = new StringBuilder();
            List<Reimbursement> approvedReimbursements = reimbursementService.getApprovedForPayroll(
                    emp.getId(), request.getMonth(), request.getYear());
            for (Reimbursement r : approvedReimbursements) {
                totalReimbursement = totalReimbursement.add(r.getAmount());
                if (reimbursementDetails.length() > 0) reimbursementDetails.append("; ");
                reimbursementDetails.append(r.getCategory()).append(": ₹").append(r.getAmount().toPlainString());
            }

            // ── 6. Gross Earnings = salary + overtime + bonus + reimbursements - LOP ──
            BigDecimal grossBeforeDeductions = basic.add(hra).add(specialAllowance);
            BigDecimal gross = grossBeforeDeductions
                    .add(overtimePay)
                    .add(totalBonus)
                    .add(totalReimbursement)
                    .subtract(lopDeduction);
            if (gross.compareTo(BigDecimal.ZERO) < 0) {
                gross = BigDecimal.ZERO;
            }

            // ── 7. PF (only if enabled by tenant) ──
            BigDecimal pfEmployee = BigDecimal.ZERO;
            BigDecimal pfEmployer = BigDecimal.ZERO;
            if (settings.isPfEnabled()) {
                BigDecimal pfRate = settings.getPfRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                pfEmployee = basic.multiply(pfRate).setScale(2, RoundingMode.HALF_UP);
                if (settings.getPfCap() != null && pfEmployee.compareTo(settings.getPfCap()) > 0) {
                    pfEmployee = settings.getPfCap();
                }
            }
            if (settings.isPfEmployerEnabled()) {
                BigDecimal pfEmpRate = settings.getPfEmployerRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                pfEmployer = basic.multiply(pfEmpRate).setScale(2, RoundingMode.HALF_UP);
                if (settings.getPfCap() != null && pfEmployer.compareTo(settings.getPfCap()) > 0) {
                    pfEmployer = settings.getPfCap();
                }
            }

            // ── 8. ESI (only if enabled and gross <= wage ceiling) ──
            BigDecimal esiEmployee = BigDecimal.ZERO;
            BigDecimal esiEmployer = BigDecimal.ZERO;
            if (settings.isEsiEnabled() && grossBeforeDeductions.compareTo(settings.getEsiWageCeiling()) <= 0) {
                esiEmployee = grossBeforeDeductions.multiply(settings.getEsiEmployeeRate()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                esiEmployer = grossBeforeDeductions.multiply(settings.getEsiEmployerRate()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // ── 9. Professional Tax (slab-based or flat amount) ──
            BigDecimal professionalTax = BigDecimal.ZERO;
            if (settings.isPtEnabled()) {
                if (settings.isPtSlabMode()) {
                    // Use slab-based PT calculation
                    professionalTax = ptSlabService.calculatePtFromSlabs(tenantId, grossBeforeDeductions);
                } else {
                    professionalTax = settings.getPtAmount();
                }
            }

            // ── 10. TDS Auto-Calculation ──
            BigDecimal tds = BigDecimal.ZERO;
            if (settings.isTdsEnabled()) {
                // Annualize gross salary for TDS calculation
                BigDecimal annualGross = grossBeforeDeductions.multiply(BigDecimal.valueOf(12));
                // Deduct annual PF for taxable income
                BigDecimal annualPf = pfEmployee.multiply(BigDecimal.valueOf(12));
                BigDecimal annualTaxableIncome = annualGross.subtract(annualPf);
                String regime = settings.getTdsRegime() != null ? settings.getTdsRegime() : "NEW";
                tds = tdsCalculationService.calculateMonthlyTds(tenantId, annualTaxableIncome, regime);
            }

            // ── 11. Loan EMI Deductions ──
            BigDecimal loanDeduction = BigDecimal.ZERO;
            StringBuilder loanDetails = new StringBuilder();
            List<EmployeeLoan> activeLoans = employeeLoanService.getActiveLoans(emp.getId());
            for (EmployeeLoan loan : activeLoans) {
                // Check if loan EMI should start this month or earlier
                boolean emiStarted = (request.getYear() > loan.getStartYear()) ||
                        (request.getYear() == loan.getStartYear() && request.getMonth() >= loan.getStartMonth());
                if (emiStarted) {
                    BigDecimal emi = employeeLoanService.processEmiDeduction(loan);
                    loanDeduction = loanDeduction.add(emi);
                    if (loanDetails.length() > 0) loanDetails.append("; ");
                    loanDetails.append(loan.getLoanType()).append(" EMI: ₹").append(emi.toPlainString());
                }
            }

            // ── 12. Total employee deductions ──
            BigDecimal deductions = pfEmployee.add(esiEmployee).add(professionalTax).add(tds).add(loanDeduction);
            BigDecimal netPay = gross.subtract(deductions);
            if (netPay.compareTo(BigDecimal.ZERO) < 0) {
                netPay = BigDecimal.ZERO;
            }

            Payslip payslip = Payslip.builder()
                    .payrollRunId(run.getId())
                    .employeeId(emp.getId())
                    .employeeName(emp.getFullName())
                    .employeeCode(emp.getEmployeeCode())
                    .grossEarnings(gross)
                    .totalDeductions(deductions)
                    .netPay(netPay)
                    .basic(basic)
                    .hra(hra)
                    .specialAllowance(specialAllowance)
                    .pfEmployee(pfEmployee)
                    .pfEmployer(pfEmployer)
                    .esiEmployee(esiEmployee)
                    .esiEmployer(esiEmployer)
                    .professionalTax(professionalTax)
                    .tds(tds)
                    .workingDays(workingDays)
                    .lopDays(lopDays)
                    .lopDeduction(lopDeduction)
                    .daysPresent(daysPresent)
                    .overtimeHours(overtimeHours)
                    .overtimePay(overtimePay)
                    .totalBonus(totalBonus)
                    .bonusDetails(bonusDetails.length() > 0 ? bonusDetails.toString() : null)
                    .loanDeduction(loanDeduction)
                    .loanDetails(loanDetails.length() > 0 ? loanDetails.toString() : null)
                    .totalReimbursement(totalReimbursement)
                    .reimbursementDetails(reimbursementDetails.length() > 0 ? reimbursementDetails.toString() : null)
                    .month(request.getMonth())
                    .year(request.getYear())
                    .build();

            payslip = payslipRepository.save(payslip);
            payslips.add(payslip);

            totalGross = totalGross.add(gross);
            totalDeductions = totalDeductions.add(deductions);
            totalNet = totalNet.add(netPay);
        }

        run.setTotalGross(totalGross);
        run.setTotalDeductions(totalDeductions);
        run.setTotalNet(totalNet);
        run.setWorkingDays(workingDays);
        run.setStatus(PayrollStatus.REVIEW);
        run = payrollRunRepository.save(run);

        // Trigger payroll generated notification event
        notificationEventService.onPayrollGenerated(tenantId, request.getMonth(), request.getYear(), payslips);

        log.info("Payroll generated for tenant={} month={}/{} employees={}",
                tenantId, request.getMonth(), request.getYear(), employees.size());

        return toRunResponse(run, true);
    }

    // ── Payroll Approval ──
    @Transactional
    public PayrollRunResponse approvePayroll(UUID runId, UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        PayrollRun run = payrollRunRepository.findByIdAndTenantId(runId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", runId));

        if (run.getStatus() != PayrollStatus.REVIEW) {
            throw new BusinessException(ErrorCodes.PAYROLL_NOT_IN_REVIEW,
                    "Only payroll in REVIEW status can be approved. Current: " + run.getStatus());
        }

        run.setStatus(PayrollStatus.COMPLETED);
        run.setApprovedBy(principal.getUserId());
        run.setApprovedAt(LocalDateTime.now());
        run = payrollRunRepository.save(run);

        log.info("Payroll approved runId={} by userId={}", runId, principal.getUserId());
        return toRunResponse(run, true);
    }

    // ── Payroll Reversal ──
    @Transactional
    public PayrollRunResponse reversePayroll(UUID runId, String reason, UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        PayrollRun run = payrollRunRepository.findByIdAndTenantId(runId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", runId));

        if (run.getStatus() != PayrollStatus.COMPLETED && run.getStatus() != PayrollStatus.REVIEW) {
            throw new BusinessException(ErrorCodes.PAYROLL_CANNOT_REVERSE,
                    "Only COMPLETED or REVIEW payroll can be reversed. Current: " + run.getStatus());
        }

        run.setStatus(PayrollStatus.REVERSED);
        run.setReversalReason(reason);
        run = payrollRunRepository.save(run);

        // Reverse any loan EMI deductions that were processed
        List<Payslip> payslips = payslipRepository.findByPayrollRunId(runId);
        // Note: Loan EMIs would need manual reversal if the payroll is reversed
        // This is flagged for the founder's attention

        log.info("Payroll reversed runId={} reason={} by userId={}", runId, reason, principal.getUserId());
        return toRunResponse(run, false);
    }

    // ── Payroll Readiness Dashboard ──
    @Transactional(readOnly = true)
    public Map<String, Object> getPayrollReadiness(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Map<String, Object> readiness = new LinkedHashMap<>();

        // Active employees count
        List<Employee> employees = employeeRepository.findByTenantIdAndStatus(tenantId, EmployeeStatus.ACTIVE);
        readiness.put("totalEmployees", employees.size());

        // Check if payroll already exists
        boolean alreadyRun = payrollRunRepository.findByTenantIdAndMonthAndYear(tenantId, month, year)
                .filter(r -> r.getStatus() != PayrollStatus.REVERSED)
                .isPresent();
        readiness.put("alreadyRun", alreadyRun);

        // Pending leave approvals
        long pendingLeaves = 0;
        // Count pending reimbursements
        List<Reimbursement> monthReimbursements = reimbursementService.getByMonth(month, year);
        long pendingReimbursements = monthReimbursements.stream()
                .filter(r -> "PENDING".equals(r.getStatus())).count();
        long approvedReimbursements = monthReimbursements.stream()
                .filter(r -> "APPROVED".equals(r.getStatus())).count();
        readiness.put("pendingReimbursements", pendingReimbursements);
        readiness.put("approvedReimbursements", approvedReimbursements);

        // Pending overtime approvals
        // Working days
        int workingDays = calculateWorkingDaysInMonth(month, year);
        int mandatoryHolidays = holidayService.getMandatoryHolidayCountInMonth(month, year);
        readiness.put("workingDays", workingDays);
        readiness.put("mandatoryHolidays", mandatoryHolidays);
        readiness.put("effectiveWorkingDays", Math.max(1, workingDays - mandatoryHolidays));

        // Active loans count
        long activeLoansCount = employees.stream()
                .mapToLong(e -> employeeLoanService.getActiveLoans(e.getId()).size())
                .sum();
        readiness.put("activeLoans", activeLoansCount);

        // Settings check
        PayrollSettings settings = payrollSettingsService.getSettingsEntity(tenantId);
        Map<String, Boolean> settingsStatus = new LinkedHashMap<>();
        settingsStatus.put("pfConfigured", settings.isPfEnabled());
        settingsStatus.put("esiConfigured", settings.isEsiEnabled());
        settingsStatus.put("ptConfigured", settings.isPtEnabled());
        settingsStatus.put("tdsConfigured", settings.isTdsEnabled());
        settingsStatus.put("overtimeConfigured", settings.isOvertimeEnabled());
        readiness.put("complianceSettings", settingsStatus);

        return readiness;
    }

    // ── Helper: Calculate LOP days from approved unpaid leaves ──
    private double calculateLopDays(UUID tenantId, UUID employeeId, int month, int year, List<LeaveType> leaveTypes) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesInRange(tenantId, startOfMonth, endOfMonth);

        double lopDays = 0;
        for (LeaveRequest lr : approvedLeaves) {
            if (!lr.getEmployeeId().equals(employeeId)) continue;

            boolean isPaid = leaveTypes.stream()
                    .filter(lt -> lt.getId().equals(lr.getLeaveTypeId()))
                    .findFirst()
                    .map(LeaveType::isPaid)
                    .orElse(true);

            if (!isPaid) {
                LocalDate effectiveStart = lr.getStartDate().isBefore(startOfMonth) ? startOfMonth : lr.getStartDate();
                LocalDate effectiveEnd = lr.getEndDate().isAfter(endOfMonth) ? endOfMonth : lr.getEndDate();
                long days = effectiveEnd.toEpochDay() - effectiveStart.toEpochDay() + 1;
                lopDays += days;
            }
        }
        return lopDays;
    }

    // ── Helper: Calculate days present from attendance records ──
    private int calculateDaysPresent(UUID tenantId, UUID employeeId, int month, int year) {
        return (int) attendanceRecordRepository.countByTenantIdAndEmployeeIdAndMonthAndYear(
                tenantId, employeeId, month, year);
    }

    // ── Helper: Sum approved overtime hours for the month ──
    private BigDecimal calculateApprovedOvertimeHours(UUID tenantId, UUID employeeId, int month, int year) {
        List<OvertimeLog> logs = overtimeLogRepository.findByTenantIdAndEmployeeIdAndMonthAndYearAndApproved(
                tenantId, employeeId, month, year, true);
        return logs.stream()
                .map(OvertimeLog::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── Helper: Calculate business days (weekdays) in a month ──
    private int calculateWorkingDaysInMonth(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        int days = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            int dow = date.getDayOfWeek().getValue(); // 1=Mon..7=Sun
            if (dow <= 5) days++;
        }
        return days;
    }

    @Transactional(readOnly = true)
    public List<PayrollRunResponse> getPayrollRuns() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return payrollRunRepository.findByTenantIdOrderByYearDescMonthDesc(tenantId).stream()
                .map(run -> toRunResponse(run, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayrollRunResponse getPayrollRun(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        PayrollRun run = payrollRunRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", id));
        return toRunResponse(run, true);
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getMyPayslips(UUID employeeId) {
        return payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId).stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] generatePayslipPdf(UUID payslipId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Payslip payslip = payslipRepository.findByIdAndTenantId(payslipId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", payslipId));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        return pdfGenerator.generate(payslip, tenant.getName());
    }

    @Transactional(readOnly = true)
    public byte[] exportPayrollToExcel(UUID runId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        PayrollRun run = payrollRunRepository.findByIdAndTenantId(runId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", runId));
        List<Payslip> payslips = payslipRepository.findByPayrollRunId(runId);
        return excelExporter.export(run, payslips);
    }

    private PayrollRunResponse toRunResponse(PayrollRun run, boolean includePayslips) {
        PayrollRunResponse.PayrollRunResponseBuilder builder = PayrollRunResponse.builder()
                .id(run.getId())
                .month(run.getMonth())
                .year(run.getYear())
                .status(run.getStatus().name())
                .runDate(run.getRunDate())
                .totalGross(run.getTotalGross())
                .totalDeductions(run.getTotalDeductions())
                .totalNet(run.getTotalNet())
                .employeeCount(run.getEmployeeCount())
                .approvedBy(run.getApprovedBy())
                .approvedAt(run.getApprovedAt())
                .reversalReason(run.getReversalReason())
                .createdAt(run.getCreatedAt());

        if (includePayslips) {
            List<PayslipResponse> payslips = payslipRepository.findByPayrollRunId(run.getId()).stream()
                    .map(this::toPayslipResponse)
                    .collect(Collectors.toList());
            builder.payslips(payslips);
        }

        return builder.build();
    }

    private PayslipResponse toPayslipResponse(Payslip p) {
        return PayslipResponse.builder()
                .id(p.getId())
                .employeeId(p.getEmployeeId())
                .employeeName(p.getEmployeeName())
                .employeeCode(p.getEmployeeCode())
                .grossEarnings(p.getGrossEarnings())
                .totalDeductions(p.getTotalDeductions())
                .netPay(p.getNetPay())
                .basic(p.getBasic())
                .hra(p.getHra())
                .specialAllowance(p.getSpecialAllowance())
                .pfEmployee(p.getPfEmployee())
                .pfEmployer(p.getPfEmployer())
                .esiEmployee(p.getEsiEmployee())
                .esiEmployer(p.getEsiEmployer())
                .professionalTax(p.getProfessionalTax())
                .tds(p.getTds())
                .otherDeductions(p.getOtherDeductions())
                .deductionRemarks(p.getDeductionRemarks())
                .workingDays(p.getWorkingDays())
                .lopDays(p.getLopDays())
                .overtimeHours(p.getOvertimeHours())
                .overtimePay(p.getOvertimePay())
                .totalBonus(p.getTotalBonus())
                .bonusDetails(p.getBonusDetails())
                .lopDeduction(p.getLopDeduction())
                .daysPresent(p.getDaysPresent())
                .loanDeduction(p.getLoanDeduction())
                .loanDetails(p.getLoanDetails())
                .totalReimbursement(p.getTotalReimbursement())
                .reimbursementDetails(p.getReimbursementDetails())
                .month(p.getMonth())
                .year(p.getYear())
                .build();
    }
}
