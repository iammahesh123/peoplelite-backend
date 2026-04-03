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
import java.text.NumberFormat;
import java.util.Locale;
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
        sendEmail(to, "Your PeopleLite Verification Code", htmlBody);
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName, String companyName) {
        String htmlBody = buildWelcomeEmailBody(fullName, companyName);
        sendEmail(to, "Welcome to PeopleLite!", htmlBody);
    }

    @Async
    public void sendEmployeeCredentialsEmail(String to, String employeeName,
                                             String companyName, String email,
                                             String password, String loginUrl) {

        String htmlBody = buildEmployeeCredentialsEmailBody(
                employeeName, companyName, email, password, loginUrl);

        sendEmail(to, "Your PeopleLite Account Credentials", htmlBody);
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
        sendEmail(to, "Reset Your PeopleLite Password", htmlBody);
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
        log.info("Subscription activated email sent for {} - plan: {}, amount: Rs.{}", companyName, planName, amount);
    }

    @Async
    public void sendTrialExpiryReminderEmail(UUID tenantId, int daysRemaining) {
        String subject = daysRemaining == 1
                ? "Your PeopleLite trial expires tomorrow!"
                : "Your PeopleLite trial expires in " + daysRemaining + " days";
        log.info("Trial expiry reminder ({} days) sent for tenant {}", daysRemaining, tenantId);
    }

    /* ========================================================= */
    /* ============= SHARED TEMPLATE COMPONENTS ================ */
    /* ========================================================= */

    /**
     * Wraps email body content in a consistent, professional base template.
     * @param headerGradient CSS gradient string for the header band
     * @param iconSvg        Inline SVG icon for the header (24x24 white)
     * @param title          Main heading in the header
     * @param subtitle       Secondary line in the header (nullable)
     * @param bodyHtml       Inner HTML for the white card body
     */
    private String wrapInBaseTemplate(String headerGradient, String iconSvg,
                                       String title, String subtitle, String bodyHtml) {
        String subtitleBlock = subtitle != null
                ? "<p style=\"margin:6px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;\">%s</p>".formatted(subtitle)
                : "";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1.0">
                <title>%s</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;-webkit-font-smoothing:antialiased;">
                <div style="max-width:600px;margin:0 auto;padding:24px 16px;">
                    <!-- Header -->
                    <div style="background:%s;border-radius:16px 16px 0 0;padding:32px 32px 28px;text-align:center;">
                        <div style="width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-block;line-height:48px;margin-bottom:16px;">
                            %s
                        </div>
                        <h1 style="margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;">%s</h1>
                        %s
                    </div>
                    <!-- Body Card -->
                    <div style="background:#ffffff;padding:32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb;">
                        %s
                    </div>
                    <!-- Footer -->
                    <div style="background:#f9fafb;border-radius:0 0 16px 16px;border:1px solid #e5e7eb;border-top:0;padding:24px 32px;text-align:center;">
                        <p style="margin:0 0 4px;font-size:13px;font-weight:600;color:#6b7280;">PeopleLite</p>
                        <p style="margin:0;font-size:11px;color:#9ca3af;">Simple HR for growing teams</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, headerGradient, iconSvg, title, subtitleBlock, bodyHtml);
    }

    /** Generates a styled CTA button. */
    private String ctaButton(String href, String label, String bgColor) {
        return """
            <div style="text-align:center;margin:28px 0;">
                <a href="%s" target="_blank" style="display:inline-block;background:%s;color:#ffffff;text-decoration:none;padding:14px 36px;border-radius:10px;font-size:15px;font-weight:600;letter-spacing:0.3px;box-shadow:0 4px 14px rgba(0,0,0,0.15);">
                    %s
                </a>
            </div>
            """.formatted(href, bgColor, label);
    }

    /** Generates a label-value row for info tables. */
    private String infoRow(String label, String value) {
        return """
            <tr>
                <td style="padding:12px 16px;font-size:13px;color:#6b7280;font-weight:500;border-bottom:1px solid #f3f4f6;width:140px;">%s</td>
                <td style="padding:12px 16px;font-size:13px;color:#111827;font-weight:600;border-bottom:1px solid #f3f4f6;">%s</td>
            </tr>
            """.formatted(label, value);
    }

    /* ========================================================= */
    /* ================= TEMPLATE BUILDERS ===================== */
    /* ========================================================= */

    private String buildOtpEmailBody(String fullName, String otpCode) {
        String icon = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='3' y='11' width='18' height='11' rx='2' ry='2'/><path d='M7 11V7a5 5 0 0 1 10 0v4'/></svg>";
        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                Use the verification code below to complete your action. This code is valid for <strong>10 minutes</strong>.
            </p>
            <div style="text-align:center;margin:24px 0;">
                <div style="display:inline-block;background:linear-gradient(135deg,#eff6ff,#dbeafe);border:2px dashed #3b82f6;border-radius:12px;padding:20px 48px;">
                    <span style="font-size:36px;font-weight:800;letter-spacing:8px;color:#1e40af;font-family:'Courier New',monospace;">%s</span>
                </div>
            </div>
            <div style="background:#fef3c7;border-left:4px solid #f59e0b;border-radius:0 8px 8px 0;padding:12px 16px;margin:24px 0;">
                <p style="margin:0;font-size:12px;color:#92400e;">If you didn't request this code, you can safely ignore this email. Never share your code with anyone.</p>
            </div>
            """.formatted(fullName, otpCode);

        return wrapInBaseTemplate(
                "linear-gradient(135deg,#2563eb,#1d4ed8)",
                icon, "Verification Code", "Secure authentication", body);
    }

    private String buildWelcomeEmailBody(String fullName, String companyName) {
        String icon = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'/><circle cx='12' cy='7' r='4'/></svg>";
        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                Welcome to <strong>%s</strong> on PeopleLite! We're thrilled to have you on board.
            </p>
            <div style="background:#f0fdf4;border-radius:12px;padding:20px 24px;margin:20px 0;">
                <p style="margin:0 0 14px;font-size:14px;font-weight:600;color:#166534;">Here's what you can do:</p>
                <table style="width:100%%;border:0;border-collapse:collapse;">
                    <tr>
                        <td style="padding:6px 0;font-size:13px;color:#15803d;vertical-align:top;width:24px;">&#10003;</td>
                        <td style="padding:6px 0;font-size:13px;color:#374151;">View and update your profile information</td>
                    </tr>
                    <tr>
                        <td style="padding:6px 0;font-size:13px;color:#15803d;vertical-align:top;">&#10003;</td>
                        <td style="padding:6px 0;font-size:13px;color:#374151;">Apply for leave and track balances</td>
                    </tr>
                    <tr>
                        <td style="padding:6px 0;font-size:13px;color:#15803d;vertical-align:top;">&#10003;</td>
                        <td style="padding:6px 0;font-size:13px;color:#374151;">Access your salary slips and documents</td>
                    </tr>
                    <tr>
                        <td style="padding:6px 0;font-size:13px;color:#15803d;vertical-align:top;">&#10003;</td>
                        <td style="padding:6px 0;font-size:13px;color:#374151;">Stay updated with company announcements</td>
                    </tr>
                </table>
            </div>
            <p style="margin:16px 0 0;font-size:13px;color:#9ca3af;line-height:1.5;">
                If you have any questions, reach out to your HR team. We're here to help!
            </p>
            """.formatted(fullName, companyName);

        return wrapInBaseTemplate(
                "linear-gradient(135deg,#7c3aed,#6d28d9)",
                icon, "Welcome to PeopleLite", "Your HR journey starts here", body);
    }

    private String buildEmployeeCredentialsEmailBody(String employeeName,
                                                     String companyName,
                                                     String email,
                                                     String password,
                                                     String loginUrl) {
        String icon = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4'/></svg>";
        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                Your account at <strong>%s</strong> has been created. Use the credentials below to sign in.
            </p>
            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;margin:20px 0;">
                <table style="width:100%%;border-collapse:collapse;">
                    %s
                    %s
                </table>
            </div>
            <div style="background:#fef2f2;border-left:4px solid #ef4444;border-radius:0 8px 8px 0;padding:12px 16px;margin:20px 0;">
                <p style="margin:0;font-size:12px;color:#991b1b;font-weight:600;">Important Security Notice</p>
                <p style="margin:4px 0 0;font-size:12px;color:#991b1b;">You will be asked to change your password on first login. Please choose a strong, unique password.</p>
            </div>
            %s
            """.formatted(
                employeeName, companyName,
                infoRow("Email", email),
                infoRow("Temporary Password", "<code style=\"background:#fef3c7;padding:3px 8px;border-radius:4px;font-size:13px;color:#92400e;font-family:'Courier New',monospace;\">" + password + "</code>"),
                ctaButton(loginUrl, "Sign In to Your Account", "linear-gradient(135deg,#4f46e5,#4338ca)")
        );

        return wrapInBaseTemplate(
                "linear-gradient(135deg,#4f46e5,#3730a3)",
                icon, "Account Created", "Your login credentials are ready", body);
    }

    private String buildSalarySlipEmailBody(String employeeName,
                                            String month,
                                            String year,
                                            BigDecimal netPay) {
        String formattedPay = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(netPay);
        String icon = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><line x1='12' y1='1' x2='12' y2='23'/><path d='M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6'/></svg>";
        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                Your salary slip for <strong>%s %s</strong> is now available.
            </p>
            <div style="text-align:center;margin:24px 0;">
                <div style="display:inline-block;background:linear-gradient(135deg,#ecfdf5,#d1fae5);border-radius:16px;padding:24px 40px;border:1px solid #a7f3d0;">
                    <p style="margin:0 0 6px;font-size:12px;font-weight:600;color:#059669;text-transform:uppercase;letter-spacing:1px;">Net Pay</p>
                    <p style="margin:0;font-size:32px;font-weight:800;color:#065f46;">%s</p>
                </div>
            </div>
            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;margin:20px 0;">
                <table style="width:100%%;border-collapse:collapse;">
                    %s
                    %s
                </table>
            </div>
            <p style="margin:16px 0 0;font-size:12px;color:#9ca3af;line-height:1.5;">
                For a detailed breakdown, please log in to your PeopleLite account and visit the Payroll section.
            </p>
            """.formatted(employeeName, month, year, formattedPay,
                infoRow("Pay Period", month + " " + year),
                infoRow("Status", "<span style=\"display:inline-block;background:#d1fae5;color:#065f46;font-size:11px;font-weight:600;padding:3px 10px;border-radius:20px;\">Processed</span>"));

        return wrapInBaseTemplate(
                "linear-gradient(135deg,#059669,#047857)",
                icon, "Salary Slip", month + " " + year, body);
    }

    private String buildPasswordResetEmailBody(String fullName, String resetLink) {
        String icon = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>";
        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                We received a request to reset your password. Click the button below to create a new password.
            </p>
            %s
            <div style="background:#fef3c7;border-left:4px solid #f59e0b;border-radius:0 8px 8px 0;padding:12px 16px;margin:24px 0;">
                <p style="margin:0;font-size:12px;color:#92400e;font-weight:600;">Link expires in 1 hour</p>
                <p style="margin:4px 0 0;font-size:12px;color:#92400e;">If you didn't request a password reset, please ignore this email. Your password will remain unchanged.</p>
            </div>
            """.formatted(fullName,
                ctaButton(resetLink, "Reset Password", "linear-gradient(135deg,#dc2626,#b91c1c)"));

        return wrapInBaseTemplate(
                "linear-gradient(135deg,#dc2626,#991b1b)",
                icon, "Password Reset", "Secure password recovery", body);
    }

    private String buildLeaveRequestNotificationEmailBody(String approverName, String employeeName,
                                                        String leaveType, String startDate,
                                                        String endDate, String reason) {
        String icon = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='3' y='4' width='18' height='18' rx='2' ry='2'/><line x1='16' y1='2' x2='16' y2='6'/><line x1='8' y1='2' x2='8' y2='6'/><line x1='3' y1='10' x2='21' y2='10'/></svg>";
        String safeReason = reason != null ? reason : "Not specified";
        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                A new leave request needs your review and approval.
            </p>
            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;margin:20px 0;">
                <table style="width:100%%;border-collapse:collapse;">
                    %s
                    %s
                    %s
                    %s
                    %s
                </table>
            </div>
            <p style="margin:16px 0 0;font-size:13px;color:#6b7280;line-height:1.5;">
                Please log in to PeopleLite to approve or reject this request.
            </p>
            """.formatted(approverName,
                infoRow("Employee", "<strong>" + employeeName + "</strong>"),
                infoRow("Leave Type", leaveType),
                infoRow("From", startDate),
                infoRow("To", endDate),
                infoRow("Reason", safeReason));

        return wrapInBaseTemplate(
                "linear-gradient(135deg,#d97706,#b45309)",
                icon, "Leave Request", "Pending your approval", body);
    }

    private String buildLeaveApprovalEmailBody(String employeeName, String leaveType,
                                               String startDate, String endDate,
                                               String status, String remarks) {
        boolean isApproved = "APPROVED".equalsIgnoreCase(status);
        String gradient = isApproved
                ? "linear-gradient(135deg,#059669,#047857)"
                : "linear-gradient(135deg,#dc2626,#991b1b)";
        String statusColor = isApproved ? "#059669" : "#dc2626";
        String statusBg = isApproved ? "#d1fae5" : "#fee2e2";
        String statusText = isApproved ? "Approved" : "Rejected";
        String icon = isApproved
                ? "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M22 11.08V12a10 10 0 1 1-5.93-9.14'/><polyline points='22 4 12 14.01 9 11.01'/></svg>"
                : "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><circle cx='12' cy='12' r='10'/><line x1='15' y1='9' x2='9' y2='15'/><line x1='9' y1='9' x2='15' y2='15'/></svg>";

        String remarksRow = remarks != null && !remarks.isBlank()
                ? infoRow("Remarks", remarks)
                : "";

        String body = """
            <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                Your leave request has been updated. Here are the details:
            </p>
            <div style="text-align:center;margin:20px 0;">
                <span style="display:inline-block;background:%s;color:%s;font-size:14px;font-weight:700;padding:8px 24px;border-radius:24px;letter-spacing:0.5px;">%s</span>
            </div>
            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;margin:20px 0;">
                <table style="width:100%%;border-collapse:collapse;">
                    %s
                    %s
                    %s
                    %s
                </table>
            </div>
            """.formatted(employeeName, statusBg, statusColor, statusText,
                infoRow("Leave Type", leaveType),
                infoRow("From", startDate),
                infoRow("To", endDate),
                remarksRow);

        return wrapInBaseTemplate(gradient, icon, "Leave " + statusText,
                "Your request has been " + statusText.toLowerCase(), body);
    }
}
