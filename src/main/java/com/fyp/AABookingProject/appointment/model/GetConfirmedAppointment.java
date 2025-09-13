package com.fyp.AABookingProject.appointment.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class GetConfirmedAppointment {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
}
