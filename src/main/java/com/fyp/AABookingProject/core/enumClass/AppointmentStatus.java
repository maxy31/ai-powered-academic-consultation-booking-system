package com.fyp.AABookingProject.core.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppointmentStatus {
    PENDING("PENDING", "Pending"),
    CONFIRMED("CONFIRMED", "Confirmed");

    public final String code;
    public final String description;
}
