package com.fyp.AABookingProject.profile.controller;

import com.fyp.AABookingProject.profile.model.*;
import com.fyp.AABookingProject.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Welcome to Profile.");
    }

    @GetMapping("/getProfileStudent")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(){
        StudentProfileResponse studentProfileResponse = profileService.getStudentProfile();
        return ResponseEntity.ok(studentProfileResponse);
    }

    @PostMapping("/editProfileStudent")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EditProfileStudentResponse> editProfileStudent(@Valid @RequestBody EditProfileStudentRequest editProfileStudentRequest, HttpServletRequest request){
        EditProfileStudentResponse response = profileService.editStudentProfile(editProfileStudentRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getProfileAdvisor")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<AdvisorProfileResponse> getAdvisorProfile(){
        AdvisorProfileResponse advisorProfileResponse = profileService.getAdvisorProfile();
        return ResponseEntity.ok(advisorProfileResponse);
    }

    @PostMapping("/editProfileAdvisor")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<EditProfileAdvisorResponse> editProfileAdvisor(@Valid @RequestBody EditProfileAdvisorRequest editProfileAdvisorRequest, HttpServletRequest request){
        EditProfileAdvisorResponse response = profileService.editAdvisorProfile(editProfileAdvisorRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();  // 使 session 失效
        return ResponseEntity.ok("Logged out");
    }
}
