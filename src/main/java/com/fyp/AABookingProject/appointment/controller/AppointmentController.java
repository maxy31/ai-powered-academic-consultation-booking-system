package com.fyp.AABookingProject.appointment.controller;

import com.fyp.AABookingProject.appointment.model.AppointmentBookedListResponse;
import com.fyp.AABookingProject.appointment.model.AppointmentCreateRequest;
import com.fyp.AABookingProject.appointment.model.AppointmentResponse;
import com.fyp.AABookingProject.appointment.model.AppointmentUpdateRequest;
import com.fyp.AABookingProject.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Welcome to Booking API.");
    }

    @PostMapping("/getAppointmentList")
    public ResponseEntity<AppointmentBookedListResponse> bookedList(@Valid @RequestBody LocalDate today){
        return ResponseEntity.ok(appointmentService.getBookedList(today));
    }

    @PostMapping("/createAppointment")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request){
        return ResponseEntity.ok(appointmentService.create(request));
    }

    @PutMapping("/editAppointment")
    public ResponseEntity<AppointmentResponse> update(@Valid @RequestBody AppointmentUpdateRequest request){
        return ResponseEntity.ok(appointmentService.update(request));
    }

    @DeleteMapping("/deleteAppointment")
    public ResponseEntity<AppointmentResponse> cancel(@Valid @RequestBody Long id){
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @PostMapping("/confirmAppointment")
    public ResponseEntity<AppointmentResponse> confirm(@Valid @RequestBody Long id){
        return ResponseEntity.ok(appointmentService.confirm(id));
    }

    @PostMapping("/rejectAppointment")
    public ResponseEntity<AppointmentResponse> reject(@Valid @RequestBody Long id){
        return ResponseEntity.ok(appointmentService.reject(id));
    }
}
