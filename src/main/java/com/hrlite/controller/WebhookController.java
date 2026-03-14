package com.hrlite.controller;

import com.hrlite.service.RazorpayService;
import com.hrlite.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final RazorpayService razorpayService;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/v1/webhooks/razorpay — Razorpay webhook endpoint.
     * Public (no JWT), but signature-verified.
     */
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("Received Razorpay webhook");

        // Verify webhook signature — mandatory for security
        if (signature == null || signature.isBlank()) {
            log.warn("Razorpay webhook received without signature header");
            return ResponseEntity.badRequest().body("Missing signature");
        }

        boolean valid = razorpayService.verifyWebhookSignature(payload, signature);
        if (!valid) {
            log.warn("Invalid Razorpay webhook signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("event").asText();

            log.info("Razorpay webhook event: {}", eventType);

            JsonNode paymentEntity = event.path("payload").path("payment").path("entity");
            String razorpayPaymentId = paymentEntity.path("id").asText();
            String razorpayOrderId = paymentEntity.path("order_id").asText();

            switch (eventType) {
                case "payment.captured" -> {
                    subscriptionService.handlePaymentCaptured(razorpayPaymentId, razorpayOrderId);
                    log.info("Webhook: payment.captured processed for order {}", razorpayOrderId);
                }
                case "payment.failed" -> {
                    String errorDescription = paymentEntity.path("error_description").asText("Payment failed");
                    subscriptionService.handlePaymentFailed(razorpayPaymentId, razorpayOrderId, errorDescription);
                    log.info("Webhook: payment.failed processed for order {}", razorpayOrderId);
                }
                default -> log.info("Webhook: unhandled event type {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: {}", e.getMessage());
            // Return 200 to prevent Razorpay from retrying
        }

        return ResponseEntity.ok("OK");
    }
}
