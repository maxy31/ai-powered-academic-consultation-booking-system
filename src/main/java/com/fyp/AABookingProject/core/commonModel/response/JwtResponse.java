package com.fyp.AABookingProject.core.commonModel.response;

import lombok.Getter;

@Getter
public class JwtResponse {
    // Getters and Setters
    private final String token;
    private final Long id;
    private final String email;
    private final String role;  // Removed roleId

    public JwtResponse(String token, Long id, String email, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
    }

}