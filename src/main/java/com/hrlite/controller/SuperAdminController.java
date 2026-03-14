package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.PagedResponse;
import com.hrlite.dtos.PlatformDashboardResponse;
import com.hrlite.dtos.PlatformMetricsResponse;
import com.hrlite.dtos.PlatformSystemHealthResponse;
import com.hrlite.dtos.PlatformTenantResponse;
import com.hrlite.service.PlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final PlatformService platformService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PlatformDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getDashboardData()));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<PlatformMetricsResponse>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getMetrics()));
    }

    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<PlatformSystemHealthResponse>> getSystemHealth() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getSystemHealth()));
    }

    @GetMapping("/tenants")
    public ResponseEntity<ApiResponse<PagedResponse<PlatformTenantResponse>>> getTenants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
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
}
