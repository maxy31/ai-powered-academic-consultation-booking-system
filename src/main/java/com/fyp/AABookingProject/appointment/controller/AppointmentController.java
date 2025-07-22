package com.fyp.AABookingProject.appointment.controller;

import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// Example of controller with role-based restrictions
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

}
