package com.fyp.AABookingProject.appointment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateAppointmentRequest {
    private Long studentId;
    private String day;
    private String startTime;
    private String endTime;
}
