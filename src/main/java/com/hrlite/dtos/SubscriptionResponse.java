package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String id;
    private UUID tenantId;
    private String tenantName;
    private String planId;
    private String planName;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime nextBillingDate;
    private LocalDateTime trialEndsAt;
    private LocalDateTime cancelledAt;
    private double mrr;
    private double arr;
    private int employeeCount;
    private int employeeLimit;
    private boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
