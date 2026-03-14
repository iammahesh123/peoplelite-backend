package com.hrlite.service;

import com.hrlite.dtos.PayrollRunResponse;
import com.hrlite.dtos.PayslipResponse;
import com.hrlite.dtos.RunPayrollRequest;
import com.hrlite.entity.PayrollRun;
import com.hrlite.entity.Payslip;
import com.hrlite.enums.PayrollStatus;
import com.hrlite.repository.PayrollRunRepository;
import com.hrlite.repository.PayslipRepository;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.TenantContext;
import com.hrlite.entity.Employee;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.excel.PayrollExcelExporter;
import com.hrlite.utils.PayslipPdfGenerator;
import com.hrlite.entity.Tenant;
import com.hrlite.entity.PayrollSettings;
import com.hrlite.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    @Transactional
    public PayrollRunResponse generatePayroll(RunPayrollRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Check if payroll already run for this month
        payrollRunRepository.findByTenantIdAndMonthAndYear(tenantId, request.getMonth(), request.getYear())
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCodes.PAYROLL_ALREADY_RUN,
                            "Payroll already generated for " + request.getMonth() + "/" + request.getYear());
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

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        List<Payslip> payslips = new ArrayList<>();

        for (Employee emp : employees) {
            BigDecimal basic = emp.getBasicSalary();
            BigDecimal hra = emp.getHra();
            BigDecimal specialAllowance = emp.getSpecialAllowance();
            BigDecimal gross = basic.add(hra).add(specialAllowance);

            // ── PF (only if enabled by tenant) ──
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

            // ── ESI (only if enabled and gross <= wage ceiling) ──
            BigDecimal esiEmployee = BigDecimal.ZERO;
            BigDecimal esiEmployer = BigDecimal.ZERO;
            if (settings.isEsiEnabled() && gross.compareTo(settings.getEsiWageCeiling()) <= 0) {
                esiEmployee = gross.multiply(settings.getEsiEmployeeRate()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                esiEmployer = gross.multiply(settings.getEsiEmployerRate()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // ── Professional Tax (only if enabled) ──
            BigDecimal professionalTax = BigDecimal.ZERO;
            if (settings.isPtEnabled()) {
                professionalTax = settings.getPtAmount();
            }

            // ── TDS (placeholder — enabled flag tracked, actual calculation TBD) ──
            BigDecimal tds = BigDecimal.ZERO;

            // ── Total employee deductions ──
            BigDecimal deductions = pfEmployee.add(esiEmployee).add(professionalTax).add(tds);
            BigDecimal netPay = gross.subtract(deductions);

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
                    .workingDays(request.getWorkingDays())
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
        run.setStatus(PayrollStatus.COMPLETED);
        run = payrollRunRepository.save(run);

        // Trigger payroll generated notification event
        notificationEventService.onPayrollGenerated(tenantId, request.getMonth(), request.getYear(), payslips);

        log.info("Payroll generated for tenant={} month={}/{} employees={}",
                tenantId, request.getMonth(), request.getYear(), employees.size());

        return toRunResponse(run, true);
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
                .month(p.getMonth())
                .year(p.getYear())
                .build();
    }
}
