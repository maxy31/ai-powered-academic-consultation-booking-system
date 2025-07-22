package com.fyp.AABookingProject.core.entity;

import com.fyp.AABookingProject.core.enumClass.BlockType;
import com.fyp.AABookingProject.core.enumClass.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "schedule_blocks")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScheduleBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "block_type")
    @Enumerated(EnumType.STRING)
    private BlockType blockType;
    @ManyToOne
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;
}
