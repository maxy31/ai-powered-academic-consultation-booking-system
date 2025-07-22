package com.fyp.AABookingProject.ocr.model;

import com.fyp.AABookingProject.core.enumClass.BlockType;
import com.fyp.AABookingProject.core.enumClass.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScheduleBlockDTO {
    private DayOfWeek day_of_week;
    private String start_time;
    private String end_time;
    private BlockType block_type;
}
