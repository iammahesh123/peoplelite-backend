package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_payment_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantPaymentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Column(name = "razorpay_key_id")
    private String razorpayKeyId;

    @Column(name = "razorpay_key_secret")
    private String razorpayKeySecret;

    @Column(name = "razorpay_webhook_secret")
    private String razorpayWebhookSecret;

    @Column(name = "billing_legal_name")
    private String billingLegalName;

    @Column(name = "billing_gstin")
    private String billingGstin;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "billing_currency")
    @Builder.Default
    private String billingCurrency = "INR";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
