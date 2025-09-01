package com.fyp.AABookingProject.notification.service;

import com.fyp.AABookingProject.notification.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationRetentionScheduler {

    private final NotificationRepository notificationRepository;

    public NotificationRetentionScheduler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Run daily at 03:15 to soft-delete read notifications older than 30 days
    @Scheduled(cron = "0 15 3 * * *")
    public void softDeleteOldRead() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        notificationRepository.softDeleteReadBefore(threshold);
    }

    // Run weekly Sunday 04:00 to permanently purge soft-deleted > 7 days
    @Scheduled(cron = "0 0 4 * * SUN")
    public void purgeDeleted() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        notificationRepository.purgeDeletedBefore(threshold);
    }
}
