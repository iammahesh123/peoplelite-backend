package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.EmployeeLoanRequest;
import com.hrlite.entity.EmployeeLoan;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.EmployeeLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class EmployeeLoanController {

    private final EmployeeLoanService employeeLoanService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<EmployeeLoan>> createLoan(
            @Valid @RequestBody EmployeeLoanRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        EmployeeLoan loan = employeeLoanService.createLoan(
                request.getEmployeeId(),
                request.getLoanType(),
                request.getPrincipalAmount(),
                request.getEmiAmount(),
                request.getStartMonth(),
                request.getStartYear(),
                request.getRemarks(),
                principal
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created", loan));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<EmployeeLoan>>> getAllLoans() {
        return ResponseEntity.ok(ApiResponse.success(employeeLoanService.getAllLoans()));
    }

    @GetMapping("/employee/{employeeId}/active")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<EmployeeLoan>>> getActiveLoans(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(employeeLoanService.getActiveLoans(employeeId)));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<EmployeeLoan>> closeLoan(@PathVariable UUID id) {
        EmployeeLoan loan = employeeLoanService.closeLoan(id);
        return ResponseEntity.ok(ApiResponse.success("Loan closed", loan));
    }
}