package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.entity.TenantContext;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * GET /api/v1/subscription/plans — Public endpoint to list active plans.
     */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        List<PlanResponse> plans = subscriptionService.getActivePlans();
        return ResponseEntity.ok(ApiResponse.success("Plans retrieved", plans));
    }

    /**
     * GET /api/v1/subscription/status — Get current tenant's subscription status.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        SubscriptionStatusResponse status = subscriptionService.getSubscriptionStatus(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * POST /api/v1/subscription/create-order — Create a Razorpay order for a plan.
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = subscriptionService.createPaymentOrder(
                principal.getTenantId(), request.getPlanId());
        return ResponseEntity.ok(ApiResponse.success("Order created", response));
    }

    /**
     * POST /api/v1/subscription/verify-payment — Verify Razorpay payment and activate subscription.
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> verifyPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {
        SubscriptionStatusResponse response = subscriptionService.verifyAndActivate(
                principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.success("Subscription activated", response));
    }

    /**
     * POST /api/v1/subscription/cancel — Cancel the tenant's subscription.
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal UserPrincipal principal) {
        subscriptionService.cancelSubscription(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled"));
    }
}
