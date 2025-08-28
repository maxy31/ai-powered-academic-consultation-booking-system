package com.fyp.AABookingProject.appointment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AppointmentBookedListResponse {
    List<AppointmentResponse> appointmentBookedList;
}
