package com.fyp.AABookingProject.appointment.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentUpdateRequest {
    @NotNull
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
