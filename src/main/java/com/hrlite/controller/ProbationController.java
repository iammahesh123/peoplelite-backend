package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.ProbationResponse;
import com.hrlite.dtos.ProbationUpdateRequest;
import com.hrlite.enums.Feature;
import com.hrlite.service.ProbationService;
import com.hrlite.service.FeatureGateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/probation")
@RequiredArgsConstructor
public class ProbationController {

    private final ProbationService probationService;
    private final FeatureGateService featureGateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProbationResponse>>> getEmployeesOnProbation() {
        featureGateService.requireFeature(Feature.PROBATION);
        List<ProbationResponse> employees = probationService.getEmployeesOnProbation();
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProbationResponse>>> getUpcomingProbationEnds(
            @RequestParam(defaultValue = "30") int days) {
        featureGateService.requireFeature(Feature.PROBATION);
        List<ProbationResponse> employees = probationService.getUpcomingProbationEnds(days);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @PutMapping("/{employeeId}/confirm")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> confirmEmployee(@PathVariable UUID employeeId) {
        featureGateService.requireFeature(Feature.PROBATION);
        probationService.confirmEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee probation confirmed"));
    }

    @PutMapping("/{employeeId}/extend")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> extendProbation(
            @PathVariable UUID employeeId,
            @Valid @RequestBody ProbationUpdateRequest request) {
        featureGateService.requireFeature(Feature.PROBATION);
        probationService.extendProbation(employeeId, request);
        return ResponseEntity.ok(ApiResponse.success("Probation period extended"));
    }
}
