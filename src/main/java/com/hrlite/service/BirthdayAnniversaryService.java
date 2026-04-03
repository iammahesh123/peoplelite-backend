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
        String companyName = tenant.getName() != null ? tenant.getName() : "our company";
        return "<!DOCTYPE html>" +
               "<html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
               "<body style='margin:0;padding:0;background-color:#f0f2f5;font-family:Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;-webkit-font-smoothing:antialiased;'>" +
               "<div style='max-width:600px;margin:0 auto;padding:24px 16px;'>" +
               // Header
               "<div style='background:linear-gradient(135deg,#ec4899,#db2777);border-radius:16px 16px 0 0;padding:32px 32px 28px;text-align:center;'>" +
               "<div style='width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-block;line-height:48px;margin-bottom:16px;'>" +
               "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z\"/></svg>" +
               "</div>" +
               "<h1 style='margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;'>Happy Birthday!</h1>" +
               "<p style='margin:6px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;'>Wishing you a wonderful day</p>" +
               "</div>" +
               // Body
               "<div style='background:#ffffff;padding:32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb;'>" +
               "<p style='margin:0 0 8px;font-size:15px;color:#374151;'>Hello <strong>" + emp.getFirstName() + "</strong>,</p>" +
               "<p style='margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;'>" +
               "Wishing you a wonderful birthday filled with joy and happiness! The entire team at <strong>" + companyName + "</strong> celebrates this special day with you.</p>" +
               "<div style='text-align:center;margin:24px 0;'>" +
               "<div style='display:inline-block;background:linear-gradient(135deg,#fdf2f8,#fce7f3);border-radius:16px;padding:24px 40px;border:1px solid #fbcfe8;'>" +
               "<p style='margin:0;font-size:40px;'>&#127874;</p>" +
               "<p style='margin:8px 0 0;font-size:14px;font-weight:600;color:#be185d;'>Have a fantastic day!</p>" +
               "</div></div>" +
               "<p style='margin:16px 0 0;font-size:14px;color:#374151;'>Warm regards,<br/><strong>The HR Team</strong></p>" +
               "</div>" +
               // Footer
               "<div style='background:#f9fafb;border-radius:0 0 16px 16px;border:1px solid #e5e7eb;border-top:0;padding:24px 32px;text-align:center;'>" +
               "<p style='margin:0 0 4px;font-size:13px;font-weight:600;color:#6b7280;'>PeopleLite</p>" +
               "<p style='margin:0;font-size:11px;color:#9ca3af;'>Simple HR for growing teams</p>" +
               "</div></div></body></html>";
    }

    private String buildAnniversaryEmail(Employee emp, long years, Tenant tenant) {
        String companyName = tenant.getName() != null ? tenant.getName() : "us";
        String yearText = years + " year" + (years > 1 ? "s" : "");
        return "<!DOCTYPE html>" +
               "<html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
               "<body style='margin:0;padding:0;background-color:#f0f2f5;font-family:Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;-webkit-font-smoothing:antialiased;'>" +
               "<div style='max-width:600px;margin:0 auto;padding:24px 16px;'>" +
               // Header
               "<div style='background:linear-gradient(135deg,#059669,#047857);border-radius:16px 16px 0 0;padding:32px 32px 28px;text-align:center;'>" +
               "<div style='width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-block;line-height:48px;margin-bottom:16px;'>" +
               "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><polygon points=\"12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2\"/></svg>" +
               "</div>" +
               "<h1 style='margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;'>Work Anniversary!</h1>" +
               "<p style='margin:6px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;'>Celebrating " + yearText + " of excellence</p>" +
               "</div>" +
               // Body
               "<div style='background:#ffffff;padding:32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb;'>" +
               "<p style='margin:0 0 8px;font-size:15px;color:#374151;'>Hello <strong>" + emp.getFirstName() + "</strong>,</p>" +
               "<p style='margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;'>" +
               "Congratulations on completing <strong>" + yearText + "</strong> with <strong>" + companyName + "</strong>!</p>" +
               "<div style='text-align:center;margin:24px 0;'>" +
               "<div style='display:inline-block;background:linear-gradient(135deg,#ecfdf5,#d1fae5);border-radius:16px;padding:24px 40px;border:1px solid #a7f3d0;'>" +
               "<p style='margin:0;font-size:40px;'>&#127942;</p>" +
               "<p style='margin:8px 0 0;font-size:24px;font-weight:800;color:#065f46;'>" + yearText + "</p>" +
               "<p style='margin:4px 0 0;font-size:12px;font-weight:600;color:#059669;text-transform:uppercase;letter-spacing:1px;'>of dedication</p>" +
               "</div></div>" +
               "<p style='margin:0 0 16px;font-size:14px;color:#6b7280;line-height:1.6;'>" +
               "Your dedication and contributions have been invaluable to our team. Thank you for being part of our journey!</p>" +
               "<p style='margin:16px 0 0;font-size:14px;color:#374151;'>Warm regards,<br/><strong>The HR Team</strong></p>" +
               "</div>" +
               // Footer
               "<div style='background:#f9fafb;border-radius:0 0 16px 16px;border:1px solid #e5e7eb;border-top:0;padding:24px 32px;text-align:center;'>" +
               "<p style='margin:0 0 4px;font-size:13px;font-weight:600;color:#6b7280;'>PeopleLite</p>" +
               "<p style='margin:0;font-size:11px;color:#9ca3af;'>Simple HR for growing teams</p>" +
               "</div></div></body></html>";
    }
}
