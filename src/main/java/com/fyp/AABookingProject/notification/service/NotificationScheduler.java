package com.fyp.AABookingProject.notification.service;

import com.fyp.AABookingProject.appointment.repository.AppointmentRepository;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(AppointmentRepository appointmentRepository, NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    // Run every 2 minutes to generate reminders for appointments starting in next 15 minutes
    @Scheduled(fixedDelay = 120000L, initialDelay = 15000L)
    public void emitUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(15);
        if (!now.toLocalDate().equals(windowEnd.toLocalDate())) {
            // Only handle same-day within this simple query strategy; if crossing midnight just return early
            windowEnd = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
        }
        List<Appointment> sameDay = appointmentRepository.findByStatusAndDateAndStartTimeBetween(
                AppointmentStatus.CONFIRMED,
                now.toLocalDate(),
                now.toLocalTime(),
                windowEnd.toLocalTime()
        );
        sameDay.forEach(notificationService::notifyReminder);
    }
}