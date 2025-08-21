package com.fyp.AABookingProject.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_entry")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 16, nullable = false)
    private String day;

    @Column(name = "start_time", length = 16)
    private String startTime;

    @Column(name = "end_time", length = 16)
    private String endTime;

    @Column(name = "grid_index")
    private Integer gridIndex;

    @Column(name = "num_slots")
    private Integer numSlots;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;
}
