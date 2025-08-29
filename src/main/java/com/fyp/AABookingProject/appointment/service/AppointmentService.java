package com.fyp.AABookingProject.appointment.service;

import com.fyp.AABookingProject.appointment.model.AppointmentBookedListResponse;
import com.fyp.AABookingProject.appointment.model.AppointmentCreateRequest;
import com.fyp.AABookingProject.appointment.model.AppointmentResponse;
import com.fyp.AABookingProject.appointment.model.AppointmentUpdateRequest;
import com.fyp.AABookingProject.appointment.repository.AppointmentRepository;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.core.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AppointmentService {
	private final AppointmentRepository appointmentRepository;
	private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
		this.userRepository = userRepository;
    }

	public AppointmentBookedListResponse getBookedList(LocalDate date){
		return null;
	}

	public AppointmentResponse create(AppointmentCreateRequest req) {
		UserDetails userDetails = getUserDetails();
		User userStudent = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (req.getStartTime().compareTo(req.getEndTime()) >= 0) {
			throw new IllegalArgumentException("End time must be after start time");
		}

		Long advisorId = userStudent.getStudent().getAdvisor().getId();
		// conflict check for advisor (any overlapping appointment not cancelled/rejected)
		appointmentRepository.findByAdvisorIdAndDate(advisorId, req.getDate()).stream()
				.filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.REJECTED)
				.filter(a -> overlaps(a.getStartTime(), a.getEndTime(), req.getStartTime(), req.getEndTime()))
				.findFirst()
				.ifPresent(a -> { throw new IllegalArgumentException("Time slot already booked by another student"); });
		// student conflict (simple loop same date)
		appointmentRepository.findByStudentIdAndDate(userStudent.getStudent().getId(), req.getDate())
				.stream()
				.filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.REJECTED)
				.filter(a -> overlaps(a.getStartTime(), a.getEndTime(), req.getStartTime(), req.getEndTime()))
				.findFirst()
				.ifPresent(a -> { throw new IllegalArgumentException("You already have an appointment overlapping this time"); });

		Appointment appt = new Appointment();
		appt.setStudentId(userStudent.getStudent().getId());
		appt.setAdvisorId(userStudent.getStudent().getAdvisor().getId());
		appt.setDate(req.getDate());
		appt.setStartTime(req.getStartTime());
		appt.setEndTime(req.getEndTime());
		appt.setStatus(AppointmentStatus.PENDING);
		appt.setCreatedAt(LocalDateTime.now().toString());
		appointmentRepository.save(appt);
		return toResponse(appt);
	}

	public AppointmentResponse confirm(Long id) {
		Appointment appt = appointmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
		appt.setStatus(AppointmentStatus.CONFIRMED);
		appointmentRepository.save(appt);
		return toResponse(appt);
	}

	public AppointmentResponse reject(Long id) {
		Appointment appt = appointmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
		appt.setStatus(AppointmentStatus.REJECTED);
		appointmentRepository.save(appt);
		return toResponse(appt);
	}

	public AppointmentResponse update(AppointmentUpdateRequest req) {
		Appointment appt = appointmentRepository.findById(req.getId())
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
		if (req.getDate() != null) appt.setDate(req.getDate());
		if (req.getStartTime() != null) appt.setStartTime(req.getStartTime());
		if (req.getEndTime() != null) appt.setEndTime(req.getEndTime());
		appointmentRepository.save(appt);
		return toResponse(appt);
	}

	public AppointmentResponse cancel(Long id) {
		Appointment appt = appointmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
		appt.setStatus(AppointmentStatus.CANCELLED);
		appointmentRepository.save(appt);
		return toResponse(appt);
	}

	private AppointmentResponse toResponse(Appointment appt) {
		return AppointmentResponse.builder()
				.id(appt.getId())
				.studentId(appt.getStudentId())
				.advisorId(appt.getAdvisorId())
				.date(appt.getDate())
				.startTime(appt.getStartTime())
				.endTime(appt.getEndTime())
				.status(appt.getStatus())
				.build();
	}

	private boolean overlaps(java.time.LocalTime aStart, java.time.LocalTime aEnd, java.time.LocalTime bStart, java.time.LocalTime bEnd){
		return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
	}

	private UserDetails getUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken) {
			throw new IllegalArgumentException("Unauthorized");
		}
		return (UserDetails) authentication.getPrincipal();
	}
}