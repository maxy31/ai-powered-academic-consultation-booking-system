package com.fyp.AABookingProject.core.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReminderType {
    APPOINTMENT_REQ("APPOINTMENT_REQUEST", "Appointment request notification"),
    APPOINTMENT_CON("APPOINTMENT_CONFIRMED", "Appointment confirmed notification"),
    APPOINTMENT_REM("APPOINTMENT_REMINDER", "Appointment reminder notification"),
    APPOINTMENT_CAN("APPOINTMENT_CANCELLATION", "Appointment cancellation notification"),
    APPOINTMENT_RES("APPOINTMENT_RESCHEDULE", "Appointment reschedule notification");

    public final String code;
    public final String description;
}
