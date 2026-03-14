package com.hrlite.controller;

import com.hrlite.dtos.PayrollSettingsRequest;
import com.hrlite.dtos.PayrollSettingsResponse;
import com.hrlite.enums.Feature;
import com.hrlite.service.PayrollSettingsService;
import com.hrlite.service.FeatureGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll/settings")
@RequiredArgsConstructor
public class PayrollSettingsController {

    private final PayrollSettingsService settingsService;
    private final FeatureGateService featureGateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<PayrollSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<PayrollSettingsResponse> updateSettings(@RequestBody PayrollSettingsRequest request) {
        featureGateService.requireFeature(Feature.PAYROLL_SETTINGS);
        return ResponseEntity.ok(settingsService.saveSettings(request));
    }
}
