package com.fyp.AABookingProject.core.entity;

import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "student_id")
    private Long studentId;
    @Column(name = "advisor_id")
    private Long advisorId;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    @Column(name = "created_at")
    private String createdAt;
    @Column(name = "meeting_notes")
    private String notes;
}
