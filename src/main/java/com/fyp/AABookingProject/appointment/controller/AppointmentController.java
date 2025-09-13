package com.fyp.AABookingProject.appointment.controller;

import com.fyp.AABookingProject.appointment.model.*;
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

    @GetMapping("/getAppointmentsList")
    public ResponseEntity<ActiveAppointmentListResponse> getAppointmentList(){
        return ResponseEntity.ok(appointmentService.getBookedList());
    }

    @GetMapping("/getLatestBooking")
    public ResponseEntity<GetConfirmedAppointment> getConfirmedAppointment(){
        return ResponseEntity.ok(appointmentService.getConfirmedAppointment());
    }

    @PostMapping("/createAppointment")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request){
        return ResponseEntity.ok(appointmentService.create(request));
    }

    @PutMapping("/editAppointment")
    public ResponseEntity<AppointmentResponse> update(@Valid @RequestBody AppointmentUpdateRequest request){
        return ResponseEntity.ok(appointmentService.update(request));
    }

    @PostMapping("/deleteAppointment")
    public ResponseEntity<AppointmentResponse> cancel(@Valid @RequestBody AdvisorHandleAppointmentRequest appointmentRequest){
        return ResponseEntity.ok(appointmentService.cancel(appointmentRequest.getAppointmentId()));
    }

    @PostMapping("/confirmAppointment")
    public ResponseEntity<AppointmentResponse> confirm(@Valid @RequestBody AdvisorHandleAppointmentRequest appointmentRequest){
        return ResponseEntity.ok(appointmentService.confirm(appointmentRequest.getAppointmentId()));
    }

    @PostMapping("/rejectAppointment")
    public ResponseEntity<AppointmentResponse> reject(@Valid @RequestBody AdvisorHandleAppointmentRequest appointmentRequest){
        return ResponseEntity.ok(appointmentService.reject(appointmentRequest.getAppointmentId()));
    }
}
