package com.hrlite.service;

import com.hrlite.entity.Subscription;
import com.hrlite.enums.SubscriptionStatus;
import com.hrlite.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    /**
     * Runs daily at midnight — expires any trial subscriptions past their trial_ends_at.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkTrialExpirations() {
        log.info("Running trial expiration check...");
        int expired = subscriptionService.checkAndExpireTrials();
        log.info("Trial expiration check complete. Expired: {}", expired);
    }

    /**
     * Runs daily at 8 AM — sends reminder emails to tenants whose trial expires in 7, 3, or 1 day(s).
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendTrialExpiryReminders() {
        log.info("Running trial expiry reminder check...");
        LocalDateTime now = LocalDateTime.now();

        // 7-day reminder
        sendRemindersForPeriod(now.plusDays(6), now.plusDays(8), 7);

        // 3-day reminder
        sendRemindersForPeriod(now.plusDays(2), now.plusDays(4), 3);

        // 1-day reminder
        sendRemindersForPeriod(now, now.plusDays(2), 1);
    }

    private void sendRemindersForPeriod(LocalDateTime start, LocalDateTime end, int daysRemaining) {
        List<Subscription> expiring = subscriptionRepository.findTrialsExpiringBetween(SubscriptionStatus.TRIAL, start, end);
        for (Subscription sub : expiring) {
            try {
                emailService.sendTrialExpiryReminderEmail(sub.getTenantId(), daysRemaining);
                log.info("Sent {}-day trial expiry reminder for tenant {}", daysRemaining, sub.getTenantId());
            } catch (Exception e) {
                log.warn("Failed to send trial expiry reminder for tenant {}: {}", sub.getTenantId(), e.getMessage());
            }
        }
    }
}
