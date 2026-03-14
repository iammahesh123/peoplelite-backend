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
public class PaymentSettingsResponse {

    private UUID id;
    private UUID tenantId;
    private String razorpayKeyId;
    private String billingLegalName;
    private String billingGstin;
    private String billingAddress;
    private String billingEmail;
    private String billingCurrency;
    private LocalDateTime updatedAt;
}
