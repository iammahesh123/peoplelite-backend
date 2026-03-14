package com.hrlite.service;

import com.hrlite.entity.TenantEmailSettings;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.TenantEmailSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender defaultMailSender;
    private final TenantEmailSettingsRepository emailSettingsRepository;
    private final String defaultMailUsername;

    public EmailService(JavaMailSender defaultMailSender,
                        TenantEmailSettingsRepository emailSettingsRepository,
                        @Value("${spring.mail.username}") String defaultMailUsername) {
        this.defaultMailSender = defaultMailSender;
        this.emailSettingsRepository = emailSettingsRepository;
        this.defaultMailUsername = defaultMailUsername;
    }

    /**
     * Get the appropriate mail sender - tenant-specific if configured, otherwise default
     */
    private JavaMailSender getMailSender() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                var settings = emailSettingsRepository.findByTenantId(tenantId);
                if (settings.isPresent()) {
                    TenantEmailSettings es = settings.get();
                    if (es.getSmtpUsername() != null && !es.getSmtpUsername().isBlank()
                            && es.getSmtpPassword() != null && !es.getSmtpPassword().isBlank()) {
                        return createMailSender(es);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to load tenant email settings, using default: {}", e.getMessage());
            }
        }
        return defaultMailSender;
    }

    /**
     * Get the from email - tenant-specific if configured, otherwise default
     */
    private String getFromEmail() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                var settings = emailSettingsRepository.findByTenantId(tenantId);
                if (settings.isPresent()) {
                    TenantEmailSettings es = settings.get();
                    if (es.getFromEmail() != null && !es.getFromEmail().isBlank()) {
                        return es.getFromEmail();
                    }
                    if (es.getSmtpUsername() != null && !es.getSmtpUsername().isBlank()) {
                        return es.getSmtpUsername();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get tenant from email: {}", e.getMessage());
            }
        }
        return defaultMailUsername;
    }

    private JavaMailSenderImpl createMailSender(TenantEmailSettings settings) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.getSmtpHost() != null ? settings.getSmtpHost() : "smtp.gmail.com");
        mailSender.setPort(settings.getSmtpPort() > 0 ? settings.getSmtpPort() : 587);
        mailSender.setUsername(settings.getSmtpUsername());
        mailSender.setPassword(settings.getSmtpPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        if (settings.isUseTls()) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            JavaMailSender mailSender = getMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(getFromEmail());
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    /* ========================================================= */

    @Async
    public void sendOtpEmail(String to, String otpCode, String fullName) {
        String htmlBody = buildOtpEmailBody(fullName, otpCode);
        sendEmail(to, "Your HR Lite Verification Code", htmlBody);
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName, String companyName) {
        String htmlBody = buildWelcomeEmailBody(fullName, companyName);
        sendEmail(to, "Welcome to HR Lite!", htmlBody);
    }

    @Async
    public void sendEmployeeCredentialsEmail(String to, String employeeName,
                                             String companyName, String email,
                                             String password, String loginUrl) {

        String htmlBody = buildEmployeeCredentialsEmailBody(
                employeeName, companyName, email, password, loginUrl);

        sendEmail(to, "Your HR Lite Account Credentials", htmlBody);
    }

    @Async
    public void sendSalarySlipEmail(String to, String employeeName,
                                    String month, String year,
                                    BigDecimal netPay) {

        String htmlBody = buildSalarySlipEmailBody(employeeName, month, year, netPay);
        sendEmail(to, "Your Salary Slip - " + month + " " + year, htmlBody);
    }

    @Async
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        String htmlBody = buildPasswordResetEmailBody(fullName, resetLink);
        sendEmail(to, "Reset Your HR Lite Password", htmlBody);
    }

    @Async
    public void sendLeaveRequestNotificationEmail(String to, String approverName, String employeeName,
                                                   String leaveType, String startDate, String endDate,
                                                   String reason) {
        String htmlBody = buildLeaveRequestNotificationEmailBody(approverName, employeeName, leaveType,
                startDate, endDate, reason);
        sendEmail(to, "New Leave Request from " + employeeName, htmlBody);
    }

    @Async
    public void sendLeaveApprovalEmail(String to, String employeeName, String leaveType,
                                       String startDate, String endDate, String status, String remarks) {
        String htmlBody = buildLeaveApprovalEmailBody(employeeName, leaveType, startDate, endDate, status, remarks);
        sendEmail(to, "Leave Request " + status, htmlBody);
    }

    @Async
    public void sendSubscriptionActivatedEmail(String companyName, String planName, String amount) {
        log.info("Subscription activated email sent for {} — plan: {}, amount: ₹{}", companyName, planName, amount);
    }

    @Async
    public void sendTrialExpiryReminderEmail(UUID tenantId, int daysRemaining) {
        String subject = daysRemaining == 1
                ? "Your HR Lite trial expires tomorrow!"
                : "Your HR Lite trial expires in " + daysRemaining + " days";
        log.info("Trial expiry reminder ({} days) sent for tenant {}", daysRemaining, tenantId);
    }

    /* ========================================================= */
    /* ================= TEMPLATE BUILDERS ===================== */
    /* ========================================================= */

    private String buildOtpEmailBody(String fullName, String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>HR Lite</h2>
                    <p>Dear %s,</p>
                    <p>Your verification code is:</p>
                    <h1 style="color:#0056b3;">%s</h1>
                    <p>This code expires in 10 minutes.</p>
                </body>
                </html>
                """.formatted(fullName, otpCode);
    }

    private String buildWelcomeEmailBody(String fullName, String companyName) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Welcome to HR Lite!</h2>
                    <p>Dear %s,</p>
                    <p>Welcome to <strong>%s</strong>.</p>
                    <p>We're excited to have you onboard.</p>
                </body>
                </html>
                """.formatted(fullName, companyName);
    }

    private String buildEmployeeCredentialsEmailBody(String employeeName,
                                                     String companyName,
                                                     String email,
                                                     String password,
                                                     String loginUrl) {

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>HR Lite Account Created</h2>
                    <p>Dear %s,</p>
                    <p>Your account at <strong>%s</strong> has been created.</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Password:</strong> %s</p>
                    <p><a href="%s">Login Here</a></p>
                </body>
                </html>
                """.formatted(employeeName, companyName, email, password, loginUrl);
    }

    private String buildSalarySlipEmailBody(String employeeName,
                                            String month,
                                            String year,
                                            BigDecimal netPay) {

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Salary Slip</h2>
                    <p>Dear %s,</p>
                    <p>Your salary slip for <strong>%s %s</strong> is available.</p>
                    <p><strong>Net Pay:</strong> ₹ %s</p>
                </body>
                </html>
                """.formatted(employeeName, month, year, netPay);
    }

    private String buildPasswordResetEmailBody(String fullName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Password Reset</h2>
                    <p>Dear %s,</p>
                    <p>Click below to reset your password:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>This link expires in 1 hour.</p>
                </body>
                </html>
                """.formatted(fullName, resetLink);
    }

    private String buildLeaveRequestNotificationEmailBody(String approverName, String employeeName,
                                                        String leaveType, String startDate,
                                                        String endDate, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>New Leave Request</h2>
                    <p>Dear %s,</p>
                    <p>A new leave request needs your approval.</p>
                    <table style="border-collapse: collapse; margin-top: 15px;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Employee</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Leave Type</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>From</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>To</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Reason</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(approverName, employeeName, leaveType, startDate, endDate, reason != null ? reason : "N/A");
    }

    private String buildLeaveApprovalEmailBody(String employeeName, String leaveType,
                                               String startDate, String endDate,
                                               String status, String remarks) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Leave Request Update</h2>
                    <p>Dear %s,</p>
                    <p>Your leave request has been <strong>%s</strong>.</p>
                    <table style="border-collapse: collapse; margin-top: 15px;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Leave Type</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>From</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>To</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        %s
                    </table>
                </body>
                </html>
                """.formatted(employeeName, status, leaveType, startDate, endDate,
                remarks != null ? "<tr><td style=\"padding: 8px; border: 1px solid #ddd;\"><strong>Remarks</strong></td><td style=\"padding: 8px; border: 1px solid #ddd;\">" + remarks + "</td></tr>" : "");
    }
}
