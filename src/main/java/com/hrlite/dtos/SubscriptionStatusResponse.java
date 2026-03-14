package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {
    private String subscriptionStatus;
    private LocalDateTime trialEndsAt;
    private long trialDaysRemaining;
    private boolean hasAccess;
    private String planName;
    private String planCode;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime nextBillingDate;
    private boolean autoRenew;
}
