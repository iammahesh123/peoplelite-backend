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
public class PaymentResponse {
    private String id;
    private String subscriptionId;
    private String tenantName;
    private String planName;
    private double amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
