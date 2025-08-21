package com.fyp.AABookingProject.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FreeSlot {
    private String day;
    private String startTime;
    private String endTime;
}
