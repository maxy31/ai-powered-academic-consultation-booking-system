package com.fyp.AABookingProject.core.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@Getter
public enum ERole implements GrantedAuthority {
    STUDENT("S", "Student"),
    ADVISOR("A", "Advisor or Lecturer"),
    ADMIN("AD", "ADMIN");

    public final String code;
    public final String description;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
