package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSettingsRequest {

    private String razorpayKeyId;
    private String razorpayKeySecret;
    private String razorpayWebhookSecret;
}
