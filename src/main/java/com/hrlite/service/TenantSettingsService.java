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
                : "PeopleLite";

        String fromEmail = settings.getFromEmail() != null
                ? settings.getFromEmail()
                : "noreply@hrlite.io";

        String subject = "PeopleLite - Test Email";

        String htmlBody = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1.0">
                <title>Test Email</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;-webkit-font-smoothing:antialiased;">
                <div style="max-width:600px;margin:0 auto;padding:24px 16px;">
                    <div style="background:linear-gradient(135deg,#2563eb,#1d4ed8);border-radius:16px 16px 0 0;padding:32px 32px 28px;text-align:center;">
                        <div style="width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-block;line-height:48px;margin-bottom:16px;">
                            <svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z'/><polyline points='22,6 12,13 2,6'/></svg>
                        </div>
                        <h1 style="margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;">Test Email</h1>
                        <p style="margin:6px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;">Email configuration check</p>
                    </div>
                    <div style="background:#ffffff;padding:32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb;">
                        <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello,</p>
                        <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                            This is a test email from PeopleLite.
                        </p>
                        <div style="text-align:center;margin:24px 0;">
                            <div style="display:inline-block;background:linear-gradient(135deg,#ecfdf5,#d1fae5);border-radius:12px;padding:20px 32px;border:1px solid #a7f3d0;">
                                <p style="margin:0 0 4px;font-size:14px;font-weight:700;color:#065f46;">&#10003; Configuration Working</p>
                                <p style="margin:0;font-size:12px;color:#059669;">Your SMTP settings are properly configured</p>
                            </div>
                        </div>
                        <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;margin:20px 0;">
                            <table style="width:100%%;border-collapse:collapse;">
                                <tr>
                                    <td style="padding:12px 16px;font-size:13px;color:#6b7280;font-weight:500;border-bottom:1px solid #f3f4f6;width:140px;">SMTP Host</td>
                                    <td style="padding:12px 16px;font-size:13px;color:#111827;font-weight:600;border-bottom:1px solid #f3f4f6;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding:12px 16px;font-size:13px;color:#6b7280;font-weight:500;border-bottom:1px solid #f3f4f6;">From</td>
                                    <td style="padding:12px 16px;font-size:13px;color:#111827;font-weight:600;border-bottom:1px solid #f3f4f6;">%s &lt;%s&gt;</td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div style="background:#f9fafb;border-radius:0 0 16px 16px;border:1px solid #e5e7eb;border-top:0;padding:24px 32px;text-align:center;">
                        <p style="margin:0 0 4px;font-size:13px;font-weight:600;color:#6b7280;">PeopleLite</p>
                        <p style="margin:0;font-size:11px;color:#9ca3af;">Simple HR for growing teams</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(smtpHost, fromName, fromEmail);

        emailService.sendEmail(recipientEmail, subject, htmlBody);
    }
}
