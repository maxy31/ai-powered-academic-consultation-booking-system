package com.fyp.AABookingProject.profile.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentProfileResponse {
    private String studentName;
    private String username;
    private String email;
    private String phoneNumber;
    private String advisorName;
}
