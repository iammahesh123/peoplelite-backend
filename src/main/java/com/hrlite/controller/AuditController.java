package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.AuditLog;
import com.hrlite.enums.Feature;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.AuditService;
import com.hrlite.service.FeatureGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final FeatureGateService featureGateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        featureGateService.requireFeature(Feature.AUDIT_TRAIL);

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<AuditLog> logs = auditService.getAuditLogs(principal.getTenantId(), action, entityType, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
