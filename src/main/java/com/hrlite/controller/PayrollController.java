package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.PayrollRunResponse;
import com.hrlite.dtos.PayslipResponse;
import com.hrlite.dtos.RunPayrollRequest;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/payroll/run")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> generatePayroll(
            @Valid @RequestBody RunPayrollRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll generated", payrollService.generatePayroll(request, principal)));
    }

    @GetMapping("/payroll/runs")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<PayrollRunResponse>>> getPayrollRuns() {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollRuns()));
    }

    @GetMapping("/payroll/runs/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> getPayrollRun(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollRun(id)));
    }

    @GetMapping("/payroll/runs/{id}/export")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<byte[]> exportPayroll(@PathVariable UUID id) {
        byte[] excel = payrollService.exportPayrollToExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-export.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/payslips/{id}/pdf")
    public ResponseEntity<byte[]> downloadPayslipPdf(@PathVariable UUID id) {
        byte[] pdf = payrollService.generatePayslipPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/payslips/me")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getMyPayslips(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getMyPayslips(principal.getEmployeeId())));
    }
}
