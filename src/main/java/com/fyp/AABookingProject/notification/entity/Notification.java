package com.fyp.AABookingProject.notification.entity;

import com.fyp.AABookingProject.notification.enumClass.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "related_appointment_id")
    private Long relatedAppointmentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // i18n support (optional). If messageKey present, client or server can re-render localized text.
    @Column(name = "message_key", length = 150)
    private String messageKey;

    // store serialized arguments (e.g. JSON array) for template interpolation
    @Column(name = "message_args", length = 500)
    private String messageArgs;

    public boolean isRead() { return readAt != null; }
    public boolean isDeleted() { return deletedAt != null; }
}
