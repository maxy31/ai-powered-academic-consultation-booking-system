package com.fyp.AABookingProject.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CandidateDto {
    private String day;
    private int slotIndex;
    private double studentFreeRate;
    private double lecturerFreeRate;
}
