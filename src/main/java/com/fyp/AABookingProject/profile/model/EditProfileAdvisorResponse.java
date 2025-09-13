package com.fyp.AABookingProject.profile.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EditProfileAdvisorResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long departmentId;
}
