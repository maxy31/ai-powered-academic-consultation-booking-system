package com.fyp.AABookingProject.appointment.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentCreateRequest {
    @NotNull
    private LocalDate date;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    private String notes;
}
