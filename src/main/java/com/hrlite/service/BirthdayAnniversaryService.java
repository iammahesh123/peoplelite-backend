package com.hrlite.service;

import com.hrlite.entity.Employee;
import com.hrlite.entity.Notification;
import com.hrlite.entity.Tenant;
import com.hrlite.entity.User;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.repository.NotificationRepository;
import com.hrlite.repository.TenantRepository;
import com.hrlite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BirthdayAnniversaryService {

    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Runs daily at 8 AM to check for birthdays and work anniversaries.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkBirthdaysAndAnniversaries() {
        log.info("Running birthday and anniversary check...");
        LocalDate today = LocalDate.now();

        List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant tenant : tenants) {
            try {
                List<Employee> employees = employeeRepository.findByTenantId(tenant.getId());
                for (Employee emp : employees) {
                    checkBirthday(emp, today, tenant);
                    checkWorkAnniversary(emp, today, tenant);
                }
            } catch (Exception e) {
                log.error("Error processing tenant {}: {}", tenant.getId(), e.getMessage());
            }
        }
    }

    private void checkBirthday(Employee emp, LocalDate today, Tenant tenant) {
        if (emp.getDateOfBirth() == null) return;

        if (emp.getDateOfBirth().getMonth() == today.getMonth() &&
            emp.getDateOfBirth().getDayOfMonth() == today.getDayOfMonth()) {

            String message = "Happy Birthday to " + emp.getFullName() + "! Wish them a great day!";

            // Notify all users in the tenant
            List<User> users = userRepository.findByTenantIdAndActiveTrue(tenant.getId());
            for (User user : users) {
                Notification notification = Notification.builder()
                        .tenantId(tenant.getId())
                        .userId(user.getId())
                        .type("BIRTHDAY")
                        .title("Birthday Celebration!")
                        .message(message)
                        .actionUrl("/employees/" + emp.getId())
                        .build();
                notificationRepository.save(notification);
            }

            // Send birthday email to the employee
            try {
                emailService.sendEmail(
                    emp.getEmail(),
                    "Happy Birthday, " + emp.getFirstName() + "!",
                    buildBirthdayEmail(emp, tenant)
                );
            } catch (Exception e) {
                log.error("Failed to send birthday email to {}: {}", emp.getEmail(), e.getMessage());
            }

            log.info("Birthday notification sent for: {} (tenant: {})", emp.getFullName(), tenant.getId());
        }
    }

    private void checkWorkAnniversary(Employee emp, LocalDate today, Tenant tenant) {
        if (emp.getDateOfJoining() == null) return;

        if (emp.getDateOfJoining().getMonth() == today.getMonth() &&
            emp.getDateOfJoining().getDayOfMonth() == today.getDayOfMonth() &&
            !emp.getDateOfJoining().isEqual(today)) {

            long years = ChronoUnit.YEARS.between(emp.getDateOfJoining(), today);
            if (years <= 0) return;

            String message = emp.getFullName() + " completes " + years + " year" + (years > 1 ? "s" : "") + " with us today! Congratulations!";

            // Notify all users in the tenant
            List<User> users = userRepository.findByTenantIdAndActiveTrue(tenant.getId());
            for (User user : users) {
                Notification notification = Notification.builder()
                        .tenantId(tenant.getId())
                        .userId(user.getId())
                        .type("ANNIVERSARY")
                        .title("Work Anniversary!")
                        .message(message)
                        .actionUrl("/employees/" + emp.getId())
                        .build();
                notificationRepository.save(notification);
            }

            // Send anniversary email
            try {
                emailService.sendEmail(
                    emp.getEmail(),
                    "Happy Work Anniversary, " + emp.getFirstName() + "!",
                    buildAnniversaryEmail(emp, years, tenant)
                );
            } catch (Exception e) {
                log.error("Failed to send anniversary email to {}: {}", emp.getEmail(), e.getMessage());
            }

            log.info("Anniversary notification sent for: {} ({} years, tenant: {})", emp.getFullName(), years, tenant.getId());
        }
    }

    private String buildBirthdayEmail(Employee emp, Tenant tenant) {
        return "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
               "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 12px 12px 0 0;'>" +
               "<h1 style='color: white; margin: 0;'>Happy Birthday!</h1>" +
               "</div>" +
               "<div style='padding: 30px; background: #fff; border: 1px solid #e5e7eb; border-top: 0; border-radius: 0 0 12px 12px;'>" +
               "<p style='font-size: 16px; color: #374151;'>Dear <strong>" + emp.getFirstName() + "</strong>,</p>" +
               "<p style='font-size: 14px; color: #6b7280;'>Wishing you a wonderful birthday filled with joy and happiness! " +
               "The entire team at " + (tenant.getName() != null ? tenant.getName() : "our company") + " celebrates this special day with you.</p>" +
               "<p style='font-size: 14px; color: #6b7280;'>Have a fantastic day!</p>" +
               "<p style='font-size: 14px; color: #374151; margin-top: 20px;'>Warm regards,<br/><strong>HR Team</strong></p>" +
               "</div></body></html>";
    }

    private String buildAnniversaryEmail(Employee emp, long years, Tenant tenant) {
        return "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
               "<div style='background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 30px; text-align: center; border-radius: 12px 12px 0 0;'>" +
               "<h1 style='color: white; margin: 0;'>Happy Work Anniversary!</h1>" +
               "</div>" +
               "<div style='padding: 30px; background: #fff; border: 1px solid #e5e7eb; border-top: 0; border-radius: 0 0 12px 12px;'>" +
               "<p style='font-size: 16px; color: #374151;'>Dear <strong>" + emp.getFirstName() + "</strong>,</p>" +
               "<p style='font-size: 14px; color: #6b7280;'>Congratulations on completing <strong>" + years + " year" + (years > 1 ? "s" : "") + "</strong> with " +
               (tenant.getName() != null ? tenant.getName() : "us") + "!</p>" +
               "<p style='font-size: 14px; color: #6b7280;'>Your dedication and contributions have been invaluable to our team. Thank you for being part of our journey!</p>" +
               "<p style='font-size: 14px; color: #374151; margin-top: 20px;'>Warm regards,<br/><strong>HR Team</strong></p>" +
               "</div></body></html>";
    }
}
