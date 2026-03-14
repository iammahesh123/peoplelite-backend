package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.SalaryRevisionRequest;
import com.hrlite.dtos.SalaryRevisionResponse;
import com.hrlite.enums.Feature;
import com.hrlite.service.SalaryRevisionService;
import com.hrlite.service.FeatureGateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salary-revisions")
@RequiredArgsConstructor
public class SalaryRevisionController {

    private final SalaryRevisionService salaryRevisionService;
    private final FeatureGateService featureGateService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<SalaryRevisionResponse>> createRevision(
            @Valid @RequestBody SalaryRevisionRequest request) {
        featureGateService.requireFeature(Feature.SALARY_REVISIONS);
        SalaryRevisionResponse response = salaryRevisionService.createRevision(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Salary revision created successfully", response));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<SalaryRevisionResponse>>> getRevisionsByEmployee(
            @PathVariable UUID employeeId) {
        featureGateService.requireFeature(Feature.SALARY_REVISIONS);
        List<SalaryRevisionResponse> revisions = salaryRevisionService.getRevisionsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(revisions));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<SalaryRevisionResponse>>> getAllRevisions() {
        featureGateService.requireFeature(Feature.SALARY_REVISIONS);
        List<SalaryRevisionResponse> revisions = salaryRevisionService.getAllRevisions();
        return ResponseEntity.ok(ApiResponse.success(revisions));
    }
}
