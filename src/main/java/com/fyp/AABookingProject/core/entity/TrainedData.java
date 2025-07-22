package com.fyp.AABookingProject.core.entity;

import com.fyp.AABookingProject.core.enumClass.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "ai_training_data")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrainedData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "student_id")
    private Long studentId;
    @Column(name = "advisor_id")
    private Long advisorId;
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    @Column(name = "time_slot")
    private LocalTime timeSlot;
    @Column(name = "label")
    private Boolean label;
    @Column(name = "features", columnDefinition = "json")
    private String features;
}
