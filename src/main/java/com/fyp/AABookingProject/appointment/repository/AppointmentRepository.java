package com.fyp.AABookingProject.appointment.repository;

import com.fyp.AABookingProject.core.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	List<Appointment> findByAdvisorIdAndDate(Long advisorId, LocalDate date);
	List<Appointment> findByStudentIdAndDate(Long studentId, LocalDate date);
	List<Appointment> findByAdvisorIdAndDateBetween(Long advisorId, LocalDate start, LocalDate end);
	List<Appointment> findByStudentIdAndDateBetween(Long studentId, LocalDate start, LocalDate end);
	List<Appointment> findByStudentId(Long studentId);
	boolean existsByDateAndStartTimeAndEndTime(LocalDate date, LocalTime startTime, LocalTime endTime);
	boolean existsByAdvisorIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long advisorId,
																					   LocalDate date,
																					   LocalTime startTime,
																					   LocalTime endTime);
	Optional<Appointment> findFirstByStudentIdAndStatusOrderByDateDescStartTimeDesc(Long studentId, AppointmentStatus status);
	List<Appointment> findByStudentIdAndStatusIn(Long studentId, Collection<AppointmentStatus> statuses);
	List<Appointment> findByAdvisorIdAndStatusIn(Long advisorId, Collection<AppointmentStatus> statuses);
	List<Appointment> findByStatusAndDateAndStartTimeBetween(AppointmentStatus status, LocalDate date, LocalTime start, LocalTime end);
}
