package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.service.PlatformSettingsService;
import com.hrlite.service.PlatformService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformSettingsController {

    private final PlatformSettingsService platformSettingsService;
    private final PlatformService platformService;

    // Feature Flags
    @GetMapping("/feature-flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getAllFeatureFlags() {
        List<FeatureFlagResponse> flags = platformSettingsService.getAllFeatureFlags();
        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    @PostMapping("/feature-flags")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> createFeatureFlag(
            @Valid @RequestBody FeatureFlagRequest request) {
        FeatureFlagResponse response = platformSettingsService.createFeatureFlag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feature flag created", response));
    }

    @PutMapping("/feature-flags/{id}")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> updateFeatureFlag(
            @PathVariable UUID id,
            @Valid @RequestBody FeatureFlagRequest request) {
        FeatureFlagResponse response = platformSettingsService.updateFeatureFlag(id, request);
        return ResponseEntity.ok(ApiResponse.success("Feature flag updated", response));
    }

    // Platform Settings
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, List<PlatformSettingResponse>>>> getAllSettings() {
        Map<String, List<PlatformSettingResponse>> settings = platformSettingsService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> updateSettings(
            @Valid @RequestBody PlatformSettingsUpdate update) {
        List<PlatformSettingResponse> response = platformSettingsService.updateSettings(update);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", response));
    }

    @PutMapping("/settings/email")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> updateEmailSettings(
            @Valid @RequestBody PlatformSettingsUpdate update) {
        List<PlatformSettingResponse> response = platformSettingsService.updateEmailSettings(update);
        return ResponseEntity.ok(ApiResponse.success("Email settings updated", response));
    }

    @PutMapping("/settings/security")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> updateSecuritySettings(
            @Valid @RequestBody PlatformSettingsUpdate update) {
        List<PlatformSettingResponse> response = platformSettingsService.updateSecuritySettings(update);
        return ResponseEntity.ok(ApiResponse.success("Security settings updated", response));
    }

    @PutMapping("/settings/integrations/razorpay")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> updateRazorpayIntegration(
            @Valid @RequestBody PlatformSettingsUpdate update) {
        List<PlatformSettingResponse> response = platformSettingsService.updateRazorpayIntegration(update);
        return ResponseEntity.ok(ApiResponse.success("Razorpay integration updated", response));
    }

    @PutMapping("/settings/integrations/google-oauth")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> updateGoogleOAuthIntegration(
            @Valid @RequestBody PlatformSettingsUpdate update) {
        List<PlatformSettingResponse> response = platformSettingsService.updateGoogleOAuthIntegration(update);
        return ResponseEntity.ok(ApiResponse.success("Google OAuth integration updated", response));
    }

    @PutMapping("/settings/integrations/s3")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> updateS3Integration(
            @Valid @RequestBody PlatformSettingsUpdate update) {
        List<PlatformSettingResponse> response = platformSettingsService.updateS3Integration(update);
        return ResponseEntity.ok(ApiResponse.success("S3 integration updated", response));
    }

    @PostMapping("/settings/test-email")
    public ResponseEntity<ApiResponse<String>> sendTestEmail() {
        platformSettingsService.sendTestEmail();
        return ResponseEntity.ok(ApiResponse.success("Test email sent successfully"));
    }

    @PutMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<PlatformTenantResponse>> updatePlan(
            @PathVariable UUID planId,
            @RequestBody Map<String, String> request) {
        String newPlan = request.getOrDefault("plan", "STARTER");
        PlatformTenantResponse response = platformService.updateTenantPlan(planId, newPlan);
        return ResponseEntity.ok(ApiResponse.success("Plan updated successfully", response));
    }

    @PutMapping("/subscriptions/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<PlatformTenantResponse>> cancelSubscription(@PathVariable UUID subscriptionId) {
        PlatformTenantResponse response = platformService.cancelSubscription(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", response));
    }

    @PutMapping("/subscriptions/{subscriptionId}/extend-trial")
    public ResponseEntity<ApiResponse<PlatformTenantResponse>> extendTrial(
            @PathVariable UUID subscriptionId,
            @RequestBody Map<String, Integer> request) {
        int days = request.getOrDefault("days", 14);
        PlatformTenantResponse response = platformService.extendTrial(subscriptionId, days);
        return ResponseEntity.ok(ApiResponse.success("Trial extended successfully", response));
    }
}
