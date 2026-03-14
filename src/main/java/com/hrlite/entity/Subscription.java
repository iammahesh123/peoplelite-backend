package com.hrlite.entity;

import com.hrlite.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "plan_id")
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Column(name = "trial_starts_at")
    private LocalDateTime trialStartsAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "razorpay_subscription_id")
    private String razorpaySubscriptionId;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private boolean autoRenew = true;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isTrialActive() {
        return status == SubscriptionStatus.TRIAL
                && trialEndsAt != null
                && trialEndsAt.isAfter(LocalDateTime.now());
    }

    public boolean isTrialExpired() {
        return status == SubscriptionStatus.TRIAL
                && trialEndsAt != null
                && trialEndsAt.isBefore(LocalDateTime.now());
    }

    public boolean hasAccess() {
        return switch (status) {
            case TRIAL -> isTrialActive();
            case ACTIVE, GRACE_PERIOD -> true;
            case EXPIRED, SUSPENDED, CANCELLED -> false;
        };
    }

    public long getTrialDaysRemaining() {
        if (trialEndsAt == null) return 0;
        long days = java.time.Duration.between(LocalDateTime.now(), trialEndsAt).toDays();
        return Math.max(0, days);
    }
}
