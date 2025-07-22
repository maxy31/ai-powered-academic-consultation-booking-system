package com.fyp.AABookingProject.core.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BlockType {
    CLASS("CLASS", "Class time"),
    OFFICE("OFFICE_HOURS", "Office time"),
    BUSY("BUSY", "Busy time"),
    FREE("FREE", "Free time");

    public final String code;
    public final String description;
}
