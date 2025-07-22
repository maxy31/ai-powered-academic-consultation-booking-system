package com.fyp.AABookingProject.core.entity;

import com.fyp.AABookingProject.core.entity.Announcement;
import com.fyp.AABookingProject.core.entity.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"announcement_id", "student_id"})
})
public class AnnouncementLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "liked_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime likedAt;
}
