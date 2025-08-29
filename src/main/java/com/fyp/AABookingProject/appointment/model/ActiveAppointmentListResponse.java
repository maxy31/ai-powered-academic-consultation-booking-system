package com.fyp.AABookingProject.appointment.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActiveAppointmentListResponse {
    private List<AppointmentResponse> appointments;
}
