package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.OnboardingService;
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
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDER')")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<OnboardingTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.getTemplates()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<OnboardingTemplateResponse>> createTemplate(
            @Valid @RequestBody OnboardingTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created", onboardingService.createTemplate(request)));
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getEmployeeOnboarding(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.getEmployeeOnboarding(employeeId)));
    }

    @PostMapping("/employees/{employeeId}/initialize")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> initializeOnboarding(
            @PathVariable UUID employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Onboarding initialized",
                        onboardingService.initializeForEmployee(employeeId)));
    }

    @PatchMapping("/tasks/{taskId}/complete")
    public ResponseEntity<ApiResponse<OnboardingTaskResponse>> completeTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Task completed",
                onboardingService.completeTask(taskId, principal)));
    }
}
