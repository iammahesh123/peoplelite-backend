package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.service.PlatformService;
import com.hrlite.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformController {

    private final PlatformService platformService;
    private final SystemHealthService systemHealthService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PlatformDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getDashboardData()));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<PlatformMetricsResponse>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getMetrics()));
    }

    @GetMapping("/system-health-old")
    public ResponseEntity<ApiResponse<PlatformSystemHealthResponse>> getSystemHealthOld() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getSystemHealth()));
    }

    @GetMapping("/tenants")
    public ResponseEntity<ApiResponse<PagedResponse<PlatformTenantResponse>>> getTenants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(platformService.getTenants(search, status, page, size)));
    }

    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<ApiResponse<PlatformTenantResponse>> getTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(platformService.getTenant(tenantId)));
    }

    @PutMapping("/tenants/{tenantId}/activate")
    public ResponseEntity<ApiResponse<PlatformTenantResponse>> activateTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success("Tenant activated", platformService.activateTenant(tenantId)));
    }

    @PutMapping("/tenants/{tenantId}/suspend")
    public ResponseEntity<ApiResponse<PlatformTenantResponse>> suspendTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success("Tenant suspended", platformService.suspendTenant(tenantId)));
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<PlansListResponse>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getPlans()));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionsListResponse>> getSubscriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(platformService.getSubscriptions(search, status)));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentsListResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.success(platformService.getPayments(page, size)));
    }

    @PutMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable String planId,
            @RequestBody PlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(platformService.updatePlan(planId, request)));
    }

    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> getSystemHealth() {
        return ResponseEntity.ok(ApiResponse.success(systemHealthService.getSystemHealth()));
    }

    @GetMapping("/system/errors")
    public ResponseEntity<ApiResponse<SystemErrorLogResponse>> getErrorLogs(
            @RequestParam(required = false) String level) {
        return ResponseEntity.ok(ApiResponse.success(systemHealthService.getErrorLogs(level)));
    }

    @GetMapping("/system/storage")
    public ResponseEntity<ApiResponse<StorageBreakdownResponse>> getStorageBreakdown() {
        return ResponseEntity.ok(ApiResponse.success(systemHealthService.getStorageBreakdown()));
    }

    @GetMapping("/system/audit")
    public ResponseEntity<ApiResponse<AuditTrailResponse>> getAuditTrail(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(systemHealthService.getAuditTrail(page, limit)));
    }
}
