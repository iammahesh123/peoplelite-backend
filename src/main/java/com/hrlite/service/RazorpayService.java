package com.hrlite.service;

import com.hrlite.config.RazorpayConfig;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    @Autowired
    public RazorpayService(@Autowired(required = false) RazorpayClient razorpayClient,
                           RazorpayConfig razorpayConfig) {
        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
    }

    /**
     * Creates a Razorpay order for the given amount.
     * Amount is in INR (rupees) — Razorpay expects paise, so we multiply by 100.
     */
    public Order createOrder(BigDecimal amount, String currency, String receiptId) {
        if (razorpayClient == null) {
            throw new BusinessException(ErrorCodes.PAYMENT_ORDER_FAILED,
                    "Payment gateway is not configured. Please contact support.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in smallest currency unit (paise for INR)
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", 1); // Auto-capture

            Order order = razorpayClient.orders.create(orderRequest);
//            log.info("Razorpay order created: {}", order.get("id"));
            return order;
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new BusinessException(ErrorCodes.PAYMENT_ORDER_FAILED,
                    "Failed to create payment order. Please try again.",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    /**
     * Verifies the Razorpay payment signature using HMAC-SHA256.
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);

            Utils.verifyPaymentSignature(attributes, razorpayConfig.getKeySecret());
            log.info("Payment signature verified for order: {}", orderId);
            return true;
        } catch (RazorpayException e) {
            log.warn("Payment signature verification failed for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifies a webhook signature from Razorpay.
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayConfig.getWebhookSecret());
            return true;
        } catch (RazorpayException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
