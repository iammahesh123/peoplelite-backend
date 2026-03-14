package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.security.UserPrincipal;
import com.hrlite.dtos.DashboardStats;
import com.hrlite.dtos.EmployeeDashboardResponse;
import com.hrlite.dtos.FounderDashboardResponse;
import com.hrlite.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStats()));
    }

    @GetMapping("/employee")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID employeeId = principal.getEmployeeId();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getEmployeeDashboard(employeeId)));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<FounderDashboardResponse>> getFounderDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getFounderDashboard()));
    }
}
