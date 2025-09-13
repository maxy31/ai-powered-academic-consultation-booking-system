package com.fyp.AABookingProject.appointment.service;

import com.fyp.AABookingProject.appointment.model.*;
import com.fyp.AABookingProject.appointment.repository.AppointmentRepository;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.notification.service.NotificationService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AppointmentService {
	private final AppointmentRepository appointmentRepository;
	private final UserRepository userRepository;

	private final NotificationService notificationService;

	public AppointmentService(AppointmentRepository appointmentRepository,
							  UserRepository userRepository,
							  NotificationService notificationService) {
		this.appointmentRepository = appointmentRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	public ActiveAppointmentListResponse getBookedList() {
		UserDetails ud = getUserDetails();
		User user = userRepository.findByUsername(ud.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		java.util.List<AppointmentStatus> activeStatuses = java.util.List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
		java.util.List<Appointment> list;
		if (user.getAdvisor() != null) {
			list = appointmentRepository.findByAdvisorIdAndStatusIn(user.getAdvisor().getId(), activeStatuses);
		} else {
			list = java.util.List.of();
		}
		var responses = list.stream().map(this::toResponse).toList();
		return new ActiveAppointmentListResponse(responses);
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
	// notify advisor
	notificationService.notifyAppointmentCreated(appt);
	return toResponse(appt);
	}

	public AppointmentResponse confirm(Long id) {
		Appointment appt = appointmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
	AppointmentStatus old = appt.getStatus();
	appt.setStatus(AppointmentStatus.CONFIRMED);
	appointmentRepository.save(appt);
	notificationService.notifyStatusChange(appt, old, AppointmentStatus.CONFIRMED);
		return toResponse(appt);
	}

	public AppointmentResponse reject(Long id) {
		Appointment appt = appointmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
	AppointmentStatus old = appt.getStatus();
	appt.setStatus(AppointmentStatus.REJECTED);
	appointmentRepository.save(appt);
	notificationService.notifyStatusChange(appt, old, AppointmentStatus.REJECTED);
		return toResponse(appt);
	}

	public AppointmentResponse update(AppointmentUpdateRequest req) {
		Appointment appt = appointmentRepository.findById(req.getId())
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
		var oldDate = appt.getDate();
		var oldStart = appt.getStartTime();
		var oldEnd = appt.getEndTime();
		boolean changed = false;
		if (req.getDate() != null && !req.getDate().equals(appt.getDate())) { appt.setDate(req.getDate()); changed = true; }
		if (req.getStartTime() != null && !req.getStartTime().equals(appt.getStartTime())) { appt.setStartTime(req.getStartTime()); changed = true; }
		if (req.getEndTime() != null && !req.getEndTime().equals(appt.getEndTime())) { appt.setEndTime(req.getEndTime()); changed = true; }
		appointmentRepository.save(appt);
		if (changed) {
			notificationService.notifyAppointmentTimeUpdated(appt, oldDate, oldStart, oldEnd);
		}
		return toResponse(appt);
	}

	public AppointmentResponse cancel(Long id) {
		Appointment appt = appointmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
	AppointmentStatus old = appt.getStatus();
	appt.setStatus(AppointmentStatus.CANCELLED);
	appointmentRepository.save(appt);
	notificationService.notifyStatusChange(appt, old, AppointmentStatus.CANCELLED);
		return toResponse(appt);
	}

	public GetConfirmedAppointment getConfirmedAppointment() {
        // 1. Get the current logged-in user
        UserDetails userDetails = getUserDetails();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Ensure the user is a student
        if (user.getStudent() == null) {
            // If the user is not a student, they don't have appointments in this context.
            return null;
        }
        Long studentId = user.getStudent().getId();

        // 3. Use the repository to find the latest confirmed appointment
        Optional<Appointment> latestAppointment = appointmentRepository
                .findFirstByStudentIdAndStatusOrderByDateDescStartTimeDesc(studentId, AppointmentStatus.CONFIRMED);

        // 4. Map the result to the DTO or return null if not found
        return latestAppointment.map(appointment -> new GetConfirmedAppointment(
                appointment.getId(),
                appointment.getDate(),
                appointment.getStartTime()
        )).orElse(null);
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