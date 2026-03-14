package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.security.UserPrincipal;
import com.hrlite.dtos.CreateEmployeeRequest;
import com.hrlite.dtos.EmployeeResponse;
import com.hrlite.dtos.UpdateEmployeeRequest;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.service.EmployeeService;
import com.hrlite.service.FeatureGateService;
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
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final FeatureGateService featureGateService;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getMyProfile(principal.getEmployeeId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployee(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        java.util.UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "Tenant context not found");
        }

        long currentEmployeeCount = employeeRepository.countByTenantId(tenantId);
        int maxEmployees = featureGateService.getMaxEmployees();

        if (currentEmployeeCount >= maxEmployees) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "You have reached the maximum number of employees (" + maxEmployees + ") for your plan. Please upgrade your plan to add more employees.");
        }

        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id, @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.updateEmployee(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable UUID id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated"));
    }
}
