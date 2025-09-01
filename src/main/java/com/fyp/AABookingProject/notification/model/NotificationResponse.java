package com.fyp.AABookingProject.notification.model;

import com.fyp.AABookingProject.notification.enumClass.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedAppointmentId;
    private LocalDateTime createdAt;
    private boolean read;
}
