package com.hrlite.service;

import com.hrlite.entity.Employee;
import com.hrlite.entity.SelfBoardingInvitation;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.repository.SelfBoardingInvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelfBoardingService {

    private final SelfBoardingInvitationRepository invitationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final OnboardingService onboardingService;

    @Transactional
    public SelfBoardingInvitation sendInvitation(UUID employeeId, String frontendBaseUrl) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String token = UUID.randomUUID().toString().replace("-", "");

        SelfBoardingInvitation invitation = SelfBoardingInvitation.builder()
                .tenantId(tenantId)
                .employeeId(employeeId)
                .token(token)
                .email(employee.getEmail())
                .status("PENDING")
                .sentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        invitation = invitationRepository.save(invitation);

        // Initialize onboarding tasks for this employee
        try {
            onboardingService.initializeForEmployee(employeeId);
        } catch (Exception e) {
            log.warn("Onboarding tasks may already be initialized for employee: {}", employeeId);
        }

        // Send email to employee
        String onboardingLink = frontendBaseUrl + "/self-onboarding?token=" + token;
        String htmlBody = buildSelfBoardingEmail(employee.getFullName(), onboardingLink);
        emailService.sendEmail(employee.getEmail(), "Complete Your Onboarding - Welcome!", htmlBody);

        log.info("Self-boarding invitation sent to {} for employee {}", employee.getEmail(), employeeId);
        return invitation;
    }

    public List<SelfBoardingInvitation> getInvitations() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return invitationRepository.findByTenantIdOrderBySentAtDesc(tenantId);
    }

    @Transactional
    public SelfBoardingInvitation completeOnboarding(String token) {
        SelfBoardingInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired invitation token"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("This invitation has already been used or expired");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new RuntimeException("This invitation has expired");
        }

        invitation.setStatus("COMPLETED");
        invitation.setCompletedAt(LocalDateTime.now());
        return invitationRepository.save(invitation);
    }

    public SelfBoardingInvitation validateToken(String token) {
        SelfBoardingInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This invitation has expired");
        }
        return invitation;
    }

    private String buildSelfBoardingEmail(String employeeName, String onboardingLink) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1.0">
                <title>Complete Your Onboarding</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;-webkit-font-smoothing:antialiased;">
                <div style="max-width:600px;margin:0 auto;padding:24px 16px;">
                    <!-- Header -->
                    <div style="background:linear-gradient(135deg,#7c3aed,#6d28d9);border-radius:16px 16px 0 0;padding:32px 32px 28px;text-align:center;">
                        <div style="width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-block;line-height:48px;margin-bottom:16px;">
                            <svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2'/><circle cx='8.5' cy='7' r='4'/><line x1='20' y1='8' x2='20' y2='14'/><line x1='23' y1='11' x2='17' y2='11'/></svg>
                        </div>
                        <h1 style="margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;">Welcome to the Team!</h1>
                        <p style="margin:6px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;">Complete your onboarding to get started</p>
                    </div>
                    <!-- Body Card -->
                    <div style="background:#ffffff;padding:32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb;">
                        <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>%s</strong>,</p>
                        <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                            We're thrilled to have you joining us! Please complete your onboarding by clicking the button below.
                        </p>
                        <div style="background:#f5f3ff;border-radius:12px;padding:20px 24px;margin:20px 0;">
                            <p style="margin:0 0 14px;font-size:14px;font-weight:600;color:#5b21b6;">What you'll need to do:</p>
                            <table style="width:100%%;border:0;border-collapse:collapse;">
                                <tr>
                                    <td style="padding:6px 0;font-size:13px;color:#7c3aed;vertical-align:top;width:24px;">&#10003;</td>
                                    <td style="padding:6px 0;font-size:13px;color:#374151;">Verify your personal information</td>
                                </tr>
                                <tr>
                                    <td style="padding:6px 0;font-size:13px;color:#7c3aed;vertical-align:top;">&#10003;</td>
                                    <td style="padding:6px 0;font-size:13px;color:#374151;">Upload required documents</td>
                                </tr>
                                <tr>
                                    <td style="padding:6px 0;font-size:13px;color:#7c3aed;vertical-align:top;">&#10003;</td>
                                    <td style="padding:6px 0;font-size:13px;color:#374151;">Acknowledge company policies</td>
                                </tr>
                            </table>
                        </div>
                        <div style="text-align:center;margin:28px 0;">
                            <a href="%s" target="_blank" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#6d28d9);color:#ffffff;text-decoration:none;padding:14px 36px;border-radius:10px;font-size:15px;font-weight:600;letter-spacing:0.3px;box-shadow:0 4px 14px rgba(0,0,0,0.15);">
                                Start Onboarding
                            </a>
                        </div>
                        <div style="background:#fef3c7;border-left:4px solid #f59e0b;border-radius:0 8px 8px 0;padding:12px 16px;margin:24px 0;">
                            <p style="margin:0;font-size:12px;color:#92400e;">This link expires in <strong>7 days</strong>. If you have any questions, please contact your HR team.</p>
                        </div>
                    </div>
                    <!-- Footer -->
                    <div style="background:#f9fafb;border-radius:0 0 16px 16px;border:1px solid #e5e7eb;border-top:0;padding:24px 32px;text-align:center;">
                        <p style="margin:0 0 4px;font-size:13px;font-weight:600;color:#6b7280;">PeopleLite</p>
                        <p style="margin:0;font-size:11px;color:#9ca3af;">Simple HR for growing teams</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(employeeName, onboardingLink);
    }
}
