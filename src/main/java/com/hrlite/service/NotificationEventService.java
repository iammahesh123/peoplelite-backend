package com.hrlite.service;

import com.hrlite.enums.Role;
import com.hrlite.entity.User;
import com.hrlite.repository.UserRepository;
import com.hrlite.entity.Notification;
import com.hrlite.repository.NotificationRepository;
import com.hrlite.entity.Payslip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Transactional
    @Async
    public void onEmployeeCreated(UUID tenantId, UUID employeeUserId, String employeeName,
                                 String founderName, String email, String rawPassword, String companyName) {
        try {
            // Create in-app notification for the employee
            Notification notification = Notification.builder()
                    .tenantId(tenantId)
                    .userId(employeeUserId)
                    .type("EMPLOYEE_ONBOARDING")
                    .title("Welcome to " + companyName + "!")
                    .message("Your account has been created. Please log in with your credentials.")
                    .actionUrl("/dashboard")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            // Send credentials email
            String loginUrl = "https://app.hrlite.io/login"; // Configure based on environment
            emailService.sendEmployeeCredentialsEmail(email, employeeName, companyName, email, rawPassword, loginUrl);

            log.info("Employee onboarding notifications sent for: {}", employeeName);
        } catch (Exception e) {
            log.error("Error in onEmployeeCreated: ", e);
        }
    }

    @Transactional
    @Async
    public void onLeaveApplied(UUID tenantId, UUID employeeId, String employeeName,
                              String leaveType, String startDate, String endDate, String reason) {
        try {
            // Find all founders in the tenant
            List<User> founders = userRepository.findByTenantIdAndRole(tenantId, Role.FOUNDER);

            // Create notification for each founder
            for (User founder : founders) {
                Notification notification = Notification.builder()
                        .tenantId(tenantId)
                        .userId(founder.getId())
                        .type("LEAVE_REQUEST")
                        .title("Leave Request from " + employeeName)
                        .message(employeeName + " requested " + leaveType + " leave from " + startDate + " to " + endDate)
                        .actionUrl("/leaves/pending")
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notification);

                // Send email to founder
                User founderUser = userRepository.findById(founder.getId()).orElse(null);
                if (founderUser != null) {
                    emailService.sendLeaveRequestNotificationEmail(founderUser.getEmail(), founderUser.getEmail(),
                            employeeName, leaveType, startDate, endDate, reason);
                }
            }

            log.info("Leave request notifications sent for: {}", employeeName);
        } catch (Exception e) {
            log.error("Error in onLeaveApplied: ", e);
        }
    }

    @Transactional
    @Async
    public void onLeaveApproved(UUID tenantId, UUID employeeUserId, String employeeName,
                               String leaveType, String startDate, String endDate, String remarks) {
        try {
            // Create in-app notification for employee
            Notification notification = Notification.builder()
                    .tenantId(tenantId)
                    .userId(employeeUserId)
                    .type("LEAVE_APPROVED")
                    .title("Leave Approved")
                    .message("Your " + leaveType + " leave has been approved.")
                    .actionUrl("/leaves/approved")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            // Send approval email to employee
            User employee = userRepository.findById(employeeUserId).orElse(null);
            if (employee != null) {
                emailService.sendLeaveApprovalEmail(employee.getEmail(), employeeName, leaveType,
                        startDate, endDate, "APPROVED", remarks);
            }

            log.info("Leave approval notifications sent for: {}", employeeName);
        } catch (Exception e) {
            log.error("Error in onLeaveApproved: ", e);
        }
    }

    @Transactional
    @Async
    public void onLeaveRejected(UUID tenantId, UUID employeeUserId, String employeeName,
                               String leaveType, String startDate, String endDate, String remarks) {
        try {
            // Create in-app notification for employee
            Notification notification = Notification.builder()
                    .tenantId(tenantId)
                    .userId(employeeUserId)
                    .type("LEAVE_REJECTED")
                    .title("Leave Rejected")
                    .message("Your " + leaveType + " leave has been rejected.")
                    .actionUrl("/leaves/rejected")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            // Send rejection email to employee
            User employee = userRepository.findById(employeeUserId).orElse(null);
            if (employee != null) {
                emailService.sendLeaveApprovalEmail(employee.getEmail(), employeeName, leaveType,
                        startDate, endDate, "REJECTED", remarks);
            }

            log.info("Leave rejection notifications sent for: {}", employeeName);
        } catch (Exception e) {
            log.error("Error in onLeaveRejected: ", e);
        }
    }

    @Transactional
    @Async
    public void onPayrollGenerated(UUID tenantId, int month, int year, List<Payslip> payslips) {
        try {
            for (Payslip payslip : payslips) {
                User employee = userRepository.findByEmployeeId(payslip.getEmployeeId()).orElse(null);
                if (employee != null) {
                    // Create in-app notification for employee
                    Notification notification = Notification.builder()
                            .tenantId(tenantId)
                            .userId(employee.getId())
                            .type("PAYROLL_READY")
                            .title("Salary Slip Ready")
                            .message("Your salary slip for " + month + "/" + year + " is ready.")
                            .actionUrl("/payroll/slips")
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notification);

                    // Send salary slip email
                    String monthName = getMonthName(month);
                    emailService.sendSalarySlipEmail(employee.getEmail(), payslip.getEmployeeName(),
                            monthName, String.valueOf(year), payslip.getNetPay());
                }
            }

            log.info("Payroll notifications sent for {}/{} - {} payslips", month, year, payslips.size());
        } catch (Exception e) {
            log.error("Error in onPayrollGenerated: ", e);
        }
    }

    @Transactional
    @Async
    public void onWelcomeRegistration(UUID tenantId, UUID userId, String fullName, String email, String companyName) {
        try {
            // Create in-app notification
            Notification notification = Notification.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .type("WELCOME")
                    .title("Welcome to PeopleLite!")
                    .message("Welcome " + fullName + "! Your organization " + companyName + " is all set up.")
                    .actionUrl("/dashboard")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            // Send welcome email
            emailService.sendWelcomeEmail(email, fullName, companyName);

            log.info("Welcome notifications sent for: {}", fullName);
        } catch (Exception e) {
            log.error("Error in onWelcomeRegistration: ", e);
        }
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return month > 0 && month <= 12 ? months[month - 1] : "Month";
    }
}
