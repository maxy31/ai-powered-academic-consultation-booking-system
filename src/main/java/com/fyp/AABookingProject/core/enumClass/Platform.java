package com.fyp.AABookingProject.core.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Platform {
    ANDROID("A", "Android Platform"),
    IOS("I", "IOS Platform");

    public final String code;
    public final String description;
}
