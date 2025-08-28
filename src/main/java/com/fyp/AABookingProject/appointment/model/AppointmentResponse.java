package com.fyp.AABookingProject.appointment.model;

import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentResponse {
    private Long id;
    private Long studentId;
    private Long advisorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
}
