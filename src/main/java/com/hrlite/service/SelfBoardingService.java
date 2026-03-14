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
            <html>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background-color: #f5f7fb;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; margin-top: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center; color: white;">
                        <h1 style="margin: 0; font-size: 28px; font-weight: 300;">Welcome to the Team!</h1>
                        <p style="margin: 10px 0 0; font-size: 16px; opacity: 0.9;">Complete your onboarding to get started</p>
                    </div>
                    <div style="padding: 30px;">
                        <p style="font-size: 16px; color: #333;">Dear <strong>%s</strong>,</p>
                        <p style="font-size: 14px; color: #555; line-height: 1.6;">
                            We're excited to have you join us! Please complete your onboarding process by clicking the button below.
                            You'll need to provide your personal information, upload required documents, and acknowledge company policies.
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea, #764ba2); color: white; text-decoration: none; padding: 14px 40px; border-radius: 8px; font-weight: 600; font-size: 16px;">
                                Start Onboarding
                            </a>
                        </div>
                        <p style="font-size: 13px; color: #888; line-height: 1.5;">
                            This link will expire in 7 days. If you have any questions, please contact your HR team.
                        </p>
                    </div>
                    <div style="background: #f8f9fa; padding: 20px 30px; text-align: center; font-size: 12px; color: #999;">
                        <p style="margin: 0;">HR Lite - Employee Self-Service Onboarding</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(employeeName, onboardingLink);
    }
}
