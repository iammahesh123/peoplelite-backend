package com.hrlite.service;

import com.hrlite.dtos.*;
import com.hrlite.entity.TenantEmailSettings;
import com.hrlite.entity.TenantNotificationSettings;
import com.hrlite.entity.TenantPaymentSettings;
import com.hrlite.repository.TenantEmailSettingsRepository;
import com.hrlite.repository.TenantNotificationSettingsRepository;
import com.hrlite.repository.TenantPaymentSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantSettingsService {

    private final TenantNotificationSettingsRepository notificationSettingsRepository;
    private final TenantEmailSettingsRepository emailSettingsRepository;
    private final TenantPaymentSettingsRepository paymentSettingsRepository;
    private final EmailService emailService;

    public NotificationSettingsResponse getNotificationSettings(UUID tenantId) {
        TenantNotificationSettings settings = notificationSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultNotificationSettings(tenantId));
        return mapToNotificationResponse(settings);
    }

    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(UUID tenantId, NotificationSettingsRequest request) {
        TenantNotificationSettings settings = notificationSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultNotificationSettings(tenantId));

        if (request.getEmailLeaveRequests() != null) settings.setEmailLeaveRequests(request.getEmailLeaveRequests());
        if (request.getEmailLeaveApprovals() != null) settings.setEmailLeaveApprovals(request.getEmailLeaveApprovals());
        if (request.getEmailPayrollReady() != null) settings.setEmailPayrollReady(request.getEmailPayrollReady());
        if (request.getEmailNewEmployee() != null) settings.setEmailNewEmployee(request.getEmailNewEmployee());
        if (request.getEmailDocumentUploaded() != null) settings.setEmailDocumentUploaded(request.getEmailDocumentUploaded());
        if (request.getEmailSystemAlerts() != null) settings.setEmailSystemAlerts(request.getEmailSystemAlerts());
        if (request.getInappLeaveRequests() != null) settings.setInappLeaveRequests(request.getInappLeaveRequests());
        if (request.getInappPayroll() != null) settings.setInappPayroll(request.getInappPayroll());
        if (request.getInappOnboarding() != null) settings.setInappOnboarding(request.getInappOnboarding());
        if (request.getInappDocuments() != null) settings.setInappDocuments(request.getInappDocuments());
        if (request.getInappSystem() != null) settings.setInappSystem(request.getInappSystem());
        if (request.getQuietHoursEnabled() != null) settings.setQuietHoursEnabled(request.getQuietHoursEnabled());
        if (request.getQuietHoursStart() != null) settings.setQuietHoursStart(request.getQuietHoursStart());
        if (request.getQuietHoursEnd() != null) settings.setQuietHoursEnd(request.getQuietHoursEnd());
        if (request.getDigestFrequency() != null) settings.setDigestFrequency(request.getDigestFrequency());

        settings.setUpdatedAt(LocalDateTime.now());
        TenantNotificationSettings saved = notificationSettingsRepository.save(settings);
        return mapToNotificationResponse(saved);
    }

    public EmailSettingsResponse getEmailSettings(UUID tenantId) {
        TenantEmailSettings settings = emailSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultEmailSettings(tenantId));
        return mapToEmailResponse(settings);
    }

    @Transactional
    public EmailSettingsResponse updateEmailSettings(UUID tenantId, EmailSettingsRequest request) {
        TenantEmailSettings settings = emailSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultEmailSettings(tenantId));

        if (request.getSmtpHost() != null) settings.setSmtpHost(request.getSmtpHost());
        if (request.getSmtpPort() != null) settings.setSmtpPort(request.getSmtpPort());
        if (request.getSmtpUsername() != null) settings.setSmtpUsername(request.getSmtpUsername());
        if (request.getSmtpPassword() != null) settings.setSmtpPassword(request.getSmtpPassword());
        if (request.getFromEmail() != null) settings.setFromEmail(request.getFromEmail());
        if (request.getFromName() != null) settings.setFromName(request.getFromName());
        if (request.getUseTls() != null) settings.setUseTls(request.getUseTls());

        settings.setUpdatedAt(LocalDateTime.now());
        TenantEmailSettings saved = emailSettingsRepository.save(settings);
        return mapToEmailResponse(saved);
    }

    public PaymentSettingsResponse getPaymentSettings(UUID tenantId) {
        TenantPaymentSettings settings = paymentSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultPaymentSettings(tenantId));
        return mapToPaymentResponse(settings);
    }

    @Transactional
    public PaymentSettingsResponse updateRazorpaySettings(UUID tenantId, PaymentSettingsRequest request) {
        TenantPaymentSettings settings = paymentSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultPaymentSettings(tenantId));

        if (request.getRazorpayKeyId() != null) settings.setRazorpayKeyId(request.getRazorpayKeyId());
        if (request.getRazorpayKeySecret() != null) settings.setRazorpayKeySecret(request.getRazorpayKeySecret());
        if (request.getRazorpayWebhookSecret() != null) settings.setRazorpayWebhookSecret(request.getRazorpayWebhookSecret());

        settings.setUpdatedAt(LocalDateTime.now());
        TenantPaymentSettings saved = paymentSettingsRepository.save(settings);
        return mapToPaymentResponse(saved);
    }

    @Transactional
    public PaymentSettingsResponse updateBillingInfo(UUID tenantId, BillingInfoRequest request) {
        TenantPaymentSettings settings = paymentSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultPaymentSettings(tenantId));

        if (request.getBillingLegalName() != null) settings.setBillingLegalName(request.getBillingLegalName());
        if (request.getBillingGstin() != null) settings.setBillingGstin(request.getBillingGstin());
        if (request.getBillingAddress() != null) settings.setBillingAddress(request.getBillingAddress());
        if (request.getBillingEmail() != null) settings.setBillingEmail(request.getBillingEmail());
        if (request.getBillingCurrency() != null) settings.setBillingCurrency(request.getBillingCurrency());

        settings.setUpdatedAt(LocalDateTime.now());
        TenantPaymentSettings saved = paymentSettingsRepository.save(settings);
        return mapToPaymentResponse(saved);
    }

    private TenantNotificationSettings createDefaultNotificationSettings(UUID tenantId) {
        TenantNotificationSettings settings = TenantNotificationSettings.builder()
                .tenantId(tenantId)
                .updatedAt(LocalDateTime.now())
                .build();
        return notificationSettingsRepository.save(settings);
    }

    private TenantEmailSettings createDefaultEmailSettings(UUID tenantId) {
        TenantEmailSettings settings = TenantEmailSettings.builder()
                .tenantId(tenantId)
                .updatedAt(LocalDateTime.now())
                .build();
        return emailSettingsRepository.save(settings);
    }

    private TenantPaymentSettings createDefaultPaymentSettings(UUID tenantId) {
        TenantPaymentSettings settings = TenantPaymentSettings.builder()
                .tenantId(tenantId)
                .updatedAt(LocalDateTime.now())
                .build();
        return paymentSettingsRepository.save(settings);
    }

    private NotificationSettingsResponse mapToNotificationResponse(TenantNotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .id(settings.getId())
                .tenantId(settings.getTenantId())
                .emailLeaveRequests(settings.isEmailLeaveRequests())
                .emailLeaveApprovals(settings.isEmailLeaveApprovals())
                .emailPayrollReady(settings.isEmailPayrollReady())
                .emailNewEmployee(settings.isEmailNewEmployee())
                .emailDocumentUploaded(settings.isEmailDocumentUploaded())
                .emailSystemAlerts(settings.isEmailSystemAlerts())
                .inappLeaveRequests(settings.isInappLeaveRequests())
                .inappPayroll(settings.isInappPayroll())
                .inappOnboarding(settings.isInappOnboarding())
                .inappDocuments(settings.isInappDocuments())
                .inappSystem(settings.isInappSystem())
                .quietHoursEnabled(settings.isQuietHoursEnabled())
                .quietHoursStart(settings.getQuietHoursStart())
                .quietHoursEnd(settings.getQuietHoursEnd())
                .digestFrequency(settings.getDigestFrequency())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private EmailSettingsResponse mapToEmailResponse(TenantEmailSettings settings) {
        return EmailSettingsResponse.builder()
                .id(settings.getId())
                .tenantId(settings.getTenantId())
                .smtpHost(settings.getSmtpHost())
                .smtpPort(settings.getSmtpPort())
                .smtpUsername(settings.getSmtpUsername())
                .fromEmail(settings.getFromEmail())
                .fromName(settings.getFromName())
                .useTls(settings.isUseTls())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private PaymentSettingsResponse mapToPaymentResponse(TenantPaymentSettings settings) {
        return PaymentSettingsResponse.builder()
                .id(settings.getId())
                .tenantId(settings.getTenantId())
                .razorpayKeyId(settings.getRazorpayKeyId())
                .billingLegalName(settings.getBillingLegalName())
                .billingGstin(settings.getBillingGstin())
                .billingAddress(settings.getBillingAddress())
                .billingEmail(settings.getBillingEmail())
                .billingCurrency(settings.getBillingCurrency())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
    public void sendTestEmail(UUID tenantId, String recipientEmail) {

        TenantEmailSettings settings = emailSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultEmailSettings(tenantId));

        String smtpHost = settings.getSmtpHost() != null
                ? settings.getSmtpHost()
                : "Default";

        String fromName = settings.getFromName() != null
                ? settings.getFromName()
                : "HR Lite";

        String fromEmail = settings.getFromEmail() != null
                ? settings.getFromEmail()
                : "noreply@hrlite.io";

        String subject = "HR Lite - Test Email";

        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #0056b3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .success-box { background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 4px; margin: 20px 0; text-align: center; }
                    .success-box p { color: #155724; margin: 5px 0; font-weight: bold; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>HR Lite</h1></div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>This is a test email from HR Lite.</p>
                        <div class="success-box">
                            <p>Your email configuration is working correctly!</p>
                        </div>
                        <p>If you received this email, your SMTP settings are properly configured.</p>
                        <p><strong>SMTP Host:</strong> %s</p>
                        <p><strong>From:</strong> %s &lt;%s&gt;</p>
                    </div>
                    <div class="footer">
                        <p><strong>HR Lite - HR for Startups</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(smtpHost, fromName, fromEmail);

        emailService.sendEmail(recipientEmail, subject, htmlBody);
    }
}
