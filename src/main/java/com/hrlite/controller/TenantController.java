package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.TenantService;
import com.hrlite.service.TenantSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final TenantSettingsService tenantSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<TenantResponse>> getCurrentTenant() {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getCurrentTenant()));
    }

    @PutMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Settings updated", tenantService.updateTenant(request)));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NotificationSettingsResponse response = tenantSettingsService.getNotificationSettings(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NotificationSettingsResponse response = tenantSettingsService.updateNotificationSettings(principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.success("Notification settings updated", response));
    }

    @GetMapping("/email")
    public ResponseEntity<ApiResponse<EmailSettingsResponse>> getEmailSettings() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        EmailSettingsResponse response = tenantSettingsService.getEmailSettings(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/email")
    public ResponseEntity<ApiResponse<EmailSettingsResponse>> updateEmailSettings(
            @Valid @RequestBody EmailSettingsRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        EmailSettingsResponse response = tenantSettingsService.updateEmailSettings(principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.success("Email settings updated", response));
    }

    @PostMapping("/email/test")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<String>> sendTestEmail() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tenantSettingsService.sendTestEmail(principal.getTenantId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Test email sent successfully"));
    }

    @GetMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentSettingsResponse>> getPaymentSettings() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PaymentSettingsResponse response = tenantSettingsService.getPaymentSettings(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/payment/razorpay")
    public ResponseEntity<ApiResponse<PaymentSettingsResponse>> updateRazorpaySettings(
            @Valid @RequestBody PaymentSettingsRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PaymentSettingsResponse response = tenantSettingsService.updateRazorpaySettings(principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.success("Razorpay settings updated", response));
    }

    @PutMapping("/payment/billing")
    public ResponseEntity<ApiResponse<PaymentSettingsResponse>> updateBillingInfo(
            @Valid @RequestBody BillingInfoRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PaymentSettingsResponse response = tenantSettingsService.updateBillingInfo(principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.success("Billing info updated", response));
    }

    @PostMapping("/branding/logo")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<TenantResponse>> uploadLogo(
            @RequestParam("file") MultipartFile file) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TenantResponse response = tenantService.uploadBrandingFile(principal.getTenantId(), file, "logo");
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", response));
    }

    @PostMapping("/branding/signature")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<TenantResponse>> uploadSignature(
            @RequestParam("file") MultipartFile file) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TenantResponse response = tenantService.uploadBrandingFile(principal.getTenantId(), file, "signature");
        return ResponseEntity.ok(ApiResponse.success("Signature uploaded successfully", response));
    }
}
