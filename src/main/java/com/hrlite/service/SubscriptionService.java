package com.hrlite.service;

import com.hrlite.config.RazorpayConfig;
import com.hrlite.dtos.*;
import com.hrlite.entity.*;
import com.hrlite.enums.PaymentStatus;
import com.hrlite.enums.SubscriptionStatus;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.*;
import com.razorpay.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final TenantRepository tenantRepository;
    private final RazorpayService razorpayService;
    private final RazorpayConfig razorpayConfig;
    private final EmailService emailService;

    // ──────────────────────────────────────────────
    // Trial Management
    // ──────────────────────────────────────────────

    @Transactional
    public Subscription initiateTrial(Tenant tenant) {
        int trialDays = razorpayConfig.getTrialDays();
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = Subscription.builder()
                .tenantId(tenant.getId())
                .status(SubscriptionStatus.TRIAL)
                .trialStartsAt(now)
                .trialEndsAt(now.plusDays(trialDays))
                .autoRenew(true)
                .build();
        subscription = subscriptionRepository.save(subscription);

        // Also update tenant for quick access
        tenant.setTrialEndsAt(now.plusDays(trialDays));
        tenantRepository.save(tenant);

        log.info("Trial initiated for tenant {} — expires at {}", tenant.getId(), subscription.getTrialEndsAt());
        return subscription;
    }

    // ──────────────────────────────────────────────
    // Subscription Status
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(UUID tenantId) {
        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElse(null);

        if (subscription == null) {
            // No subscription record — treat as expired trial for legacy tenants
            return SubscriptionStatusResponse.builder()
                    .subscriptionStatus("EXPIRED")
                    .trialDaysRemaining(0)
                    .hasAccess(false)
                    .build();
        }

        // If trial and expired, auto-mark as EXPIRED
        if (subscription.isTrialExpired()) {
            return SubscriptionStatusResponse.builder()
                    .subscriptionStatus(SubscriptionStatus.EXPIRED.name())
                    .trialEndsAt(subscription.getTrialEndsAt())
                    .trialDaysRemaining(0)
                    .hasAccess(false)
                    .build();
        }

        String planName = null;
        String planCode = null;
        if (subscription.getPlanId() != null) {
            Plan plan = planRepository.findById(subscription.getPlanId()).orElse(null);
            if (plan != null) {
                planName = plan.getName();
                planCode = plan.getCode();
            }
        }

        return SubscriptionStatusResponse.builder()
                .subscriptionStatus(subscription.getStatus().name())
                .trialEndsAt(subscription.getTrialEndsAt())
                .trialDaysRemaining(subscription.getTrialDaysRemaining())
                .hasAccess(subscription.hasAccess())
                .planName(planName)
                .planCode(planCode)
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .nextBillingDate(subscription.getNextBillingDate())
                .autoRenew(subscription.isAutoRenew())
                .build();
    }

    // ──────────────────────────────────────────────
    // Plans
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PlanResponse> getActivePlans() {
        return planRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapPlanToResponse)
                .toList();
    }

    // ──────────────────────────────────────────────
    // Razorpay Order Creation
    // ──────────────────────────────────────────────

    @Transactional
    public CreateOrderResponse createPaymentOrder(UUID tenantId, UUID planId) {
        Plan plan = planRepository.findByIdAndActiveTrue(planId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.PLAN_NOT_FOUND,
                        "Plan not found or inactive", HttpStatus.NOT_FOUND));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND,
                        "Tenant not found", HttpStatus.NOT_FOUND));

        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElse(null);

        // Create Razorpay order
        String receiptId = "rcpt_" + tenantId.toString().substring(0, 8) + "_" + System.currentTimeMillis();
        Order razorpayOrder = razorpayService.createOrder(plan.getPrice(), plan.getCurrency(), receiptId);

        String orderId = razorpayOrder.get("id");

        // Save payment record
        PaymentRecord payment = PaymentRecord.builder()
                .subscriptionId(subscription != null ? subscription.getId() : null)
                .tenantId(tenantId)
                .razorpayOrderId(orderId)
                .amount(plan.getPrice())
                .currency(plan.getCurrency())
                .status(PaymentStatus.INITIATED)
                .build();
        paymentRecordRepository.save(payment);

        log.info("Payment order created for tenant {} plan {} — orderId: {}", tenantId, plan.getCode(), orderId);

        return CreateOrderResponse.builder()
                .orderId(orderId)
                .amount(plan.getPrice())
                .currency(plan.getCurrency())
                .planId(plan.getId().toString())
                .planName(plan.getName())
                .razorpayKeyId(razorpayConfig.getKeyId())
                .build();
    }

    // ──────────────────────────────────────────────
    // Payment Verification & Subscription Activation
    // ──────────────────────────────────────────────

    @Transactional
    public SubscriptionStatusResponse verifyAndActivate(UUID tenantId, VerifyPaymentRequest request) {
        // 1. Verify Razorpay signature
        boolean valid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        if (!valid) {
            throw new BusinessException(ErrorCodes.PAYMENT_VERIFICATION_FAILED,
                    "Payment verification failed. Please try again.", HttpStatus.BAD_REQUEST);
        }

        // 2. Find and update payment record
        PaymentRecord payment = paymentRecordRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.PAYMENT_VERIFICATION_FAILED,
                        "Payment record not found", HttpStatus.NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCodes.PAYMENT_ALREADY_PROCESSED,
                    "This payment has already been processed", HttpStatus.CONFLICT);
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRecordRepository.save(payment);

        // 3. Determine the plan from the payment amount
        Plan plan = planRepository.findAll().stream()
                .filter(p -> p.getPrice().compareTo(payment.getAmount()) == 0 && p.isActive())
                .findFirst()
                .orElse(null);

        // 4. Update or create subscription
        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElse(Subscription.builder().tenantId(tenantId).build());

        LocalDateTime now = LocalDateTime.now();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPlanId(plan != null ? plan.getId() : null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));
        subscription.setUpdatedAt(now);
        subscriptionRepository.save(subscription);

        // 5. Update tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "Tenant not found"));
        tenant.setPlan(plan != null ? plan.getCode() : "STARTER");
        tenant.setActive(true);
        tenantRepository.save(tenant);

        log.info("Subscription activated for tenant {} — plan: {}", tenantId,
                plan != null ? plan.getCode() : "UNKNOWN");

        // 6. Send notification
        try {
            emailService.sendSubscriptionActivatedEmail(
                    tenant.getName(),
                    plan != null ? plan.getName() : "Subscription",
                    payment.getAmount().toString());
        } catch (Exception e) {
            log.warn("Failed to send subscription activation email: {}", e.getMessage());
        }

        return getSubscriptionStatus(tenantId);
    }

    // ──────────────────────────────────────────────
    // Webhook handlers
    // ──────────────────────────────────────────────

    @Transactional
    public void handlePaymentCaptured(String razorpayPaymentId, String razorpayOrderId) {
        PaymentRecord payment = paymentRecordRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);
        if (payment == null) {
            log.warn("Webhook: payment record not found for order {}", razorpayOrderId);
            return;
        }
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Webhook: payment {} already completed, skipping", razorpayOrderId);
            return;
        }
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRecordRepository.save(payment);
        log.info("Webhook: payment captured for order {}", razorpayOrderId);
    }

    @Transactional
    public void handlePaymentFailed(String razorpayPaymentId, String razorpayOrderId, String errorMessage) {
        PaymentRecord payment = paymentRecordRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);
        if (payment == null) {
            log.warn("Webhook: payment record not found for order {}", razorpayOrderId);
            return;
        }
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(errorMessage);
        paymentRecordRepository.save(payment);
        log.info("Webhook: payment failed for order {} — {}", razorpayOrderId, errorMessage);
    }

    // ──────────────────────────────────────────────
    // Cancellation
    // ──────────────────────────────────────────────

    @Transactional
    public void cancelSubscription(UUID tenantId) {
        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                        "No subscription found", HttpStatus.NOT_FOUND));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setAutoRenew(false);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled for tenant {}", tenantId);
    }

    // ──────────────────────────────────────────────
    // Trial Expiration (called by scheduler)
    // ──────────────────────────────────────────────

    @Transactional
    public int checkAndExpireTrials() {
        List<Subscription> expiredTrials = subscriptionRepository
                .findExpiredTrials(SubscriptionStatus.TRIAL, LocalDateTime.now());

        for (Subscription sub : expiredTrials) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(sub);

            // Mark tenant as inactive
            tenantRepository.findById(sub.getTenantId()).ifPresent(tenant -> {
                tenant.setActive(false);
                tenantRepository.save(tenant);
                log.info("Trial expired for tenant {} ({})", tenant.getId(), tenant.getName());
            });
        }

        if (!expiredTrials.isEmpty()) {
            log.info("Expired {} trial subscriptions", expiredTrials.size());
        }
        return expiredTrials.size();
    }

    // ──────────────────────────────────────────────
    // Admin Operations (Platform)
    // ──────────────────────────────────────────────

    @Transactional
    public void extendTrial(UUID subscriptionId, int days) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                        "Subscription not found", HttpStatus.NOT_FOUND));

        if (subscription.getStatus() != SubscriptionStatus.TRIAL
                && subscription.getStatus() != SubscriptionStatus.EXPIRED) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Can only extend trial for TRIAL or EXPIRED subscriptions");
        }

        LocalDateTime newTrialEnd = subscription.getTrialEndsAt() != null
                ? subscription.getTrialEndsAt().plusDays(days)
                : LocalDateTime.now().plusDays(days);

        subscription.setTrialEndsAt(newTrialEnd);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // Update tenant too
        tenantRepository.findById(subscription.getTenantId()).ifPresent(tenant -> {
            tenant.setTrialEndsAt(newTrialEnd);
            tenant.setActive(true);
            tenantRepository.save(tenant);
        });

        log.info("Trial extended by {} days for subscription {}", days, subscriptionId);
    }

    @Transactional
    public void suspendSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                        "Subscription not found", HttpStatus.NOT_FOUND));

        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        tenantRepository.findById(subscription.getTenantId()).ifPresent(tenant -> {
            tenant.setActive(false);
            tenantRepository.save(tenant);
        });

        log.info("Subscription suspended: {}", subscriptionId);
    }

    @Transactional
    public void reactivateSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                        "Subscription not found", HttpStatus.NOT_FOUND));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCancelledAt(null);
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        tenantRepository.findById(subscription.getTenantId()).ifPresent(tenant -> {
            tenant.setActive(true);
            tenantRepository.save(tenant);
        });

        log.info("Subscription reactivated: {}", subscriptionId);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private PlanResponse mapPlanToResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId().toString())
                .name(plan.getName())
                .code(plan.getCode())
                .price(plan.getPrice().doubleValue())
                .billingCycle("MONTHLY")
                .employeeLimit(plan.getMaxEmployees())
                .features(plan.getFeaturesList())
                .description(plan.getDescription())
                .active(plan.isActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
