package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.TdsSlab;
import com.hrlite.entity.TenantContext;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.TdsCalculationService;
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
@RequestMapping("/api/v1/tds-slabs")
@RequiredArgsConstructor
public class TdsSlabController {

    private final TdsCalculationService tdsCalculationService;

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<TdsSlab>>> getSlabs() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(ApiResponse.success(tdsCalculationService.getSlabs(tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<TdsSlab>>> saveSlabs(
            @Valid @RequestBody List<TdsSlab> slabs,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<TdsSlab> savedSlabs = tdsCalculationService.saveSlabs(tenantId, slabs);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("TDS slabs saved", savedSlabs));
    }
}
