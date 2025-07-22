package com.fyp.AABookingProject.core.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DayOfWeek {
    MON("MON", "Monday"),
    TUE("TUE", "Tuesday"),
    WED("WED", "Wednesday"),
    THU("THU", "Thursday"),
    FRI("FRI", "Friday"),
    SAT("SAT", "Saturday"),
    SUN("SUN", "Sunday");

    public final String code;
    public final String description;
}
