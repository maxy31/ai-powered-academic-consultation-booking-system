package com.fyp.AABookingProject.core.entity;

import com.fyp.AABookingProject.core.enumClass.ReminderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "title")
    private String title;
    @Column(name = "message")
    private String message;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ReminderType type;
    @Column(name = "related_entity_id")
    private Long relatedId;
    @Column(name = "is_read")
    private Boolean isRead;
    @Column(name = "created_at")
    private String createdAt;
    @Column(name = "scheduled_at")
    private String scheduledAt;
}
