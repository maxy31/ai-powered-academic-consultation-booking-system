package com.fyp.AABookingProject.appointment.service;

import com.fyp.AABookingProject.appointment.model.AppointmentCreateRequest;
import com.fyp.AABookingProject.appointment.model.AppointmentResponse;
import com.fyp.AABookingProject.appointment.repository.AppointmentRepository;
import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User.UserBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    AppointmentRepository appointmentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    AppointmentService service;

    @BeforeEach
    void setUpAuth() {
        UserBuilder ub = org.springframework.security.core.userdetails.User.withUsername("stu");
        UserDetails ud = ub.password("x").authorities(List.of()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private User setupStudentUserGraph(Long studentId, Long advisorId) {
        User u = new User();
        u.setId(1L);
        u.setUsername("stu");
        Student s = new Student();
        s.setId(studentId);
        Advisor a = new Advisor();
        a.setId(advisorId);
        s.setAdvisor(a);
        u.setStudent(s);
        return u;
    }

    @Test
    void create_shouldReject_whenAdvisorSlotOverlaps() {
        // Given current user is student with advisor
        when(userRepository.findByUsername("stu")).thenReturn(Optional.of(setupStudentUserGraph(10L, 20L)));

        // Existing appointment for advisor overlapping 09:00-09:30 on date
        LocalDate date = LocalDate.of(2099, 1, 15);
        Appointment existing = new Appointment();
        existing.setAdvisorId(20L);
        existing.setDate(date);
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setEndTime(LocalTime.of(9, 30));
        existing.setStatus(AppointmentStatus.CONFIRMED);
    when(appointmentRepository.findByAdvisorIdAndDate(20L, date)).thenReturn(List.of(existing));

        AppointmentCreateRequest req = new AppointmentCreateRequest(date, LocalTime.of(9, 0), LocalTime.of(9, 30), null);

        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(req));
        assertTrue(ex.getMessage().toLowerCase().contains("already booked"));
        verify(notificationService, never()).notifyAppointmentCreated(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void confirm_shouldUpdateStatus_andNotify() {
        // Given
        Appointment appt = new Appointment();
        appt.setId(777L);
        appt.setStatus(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(777L)).thenReturn(Optional.of(appt));

        // When
        AppointmentResponse resp = service.confirm(777L);

        // Then
        assertEquals(AppointmentStatus.CONFIRMED, resp.getStatus());
        ArgumentCaptor<Appointment> savedCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(savedCaptor.capture());
        assertEquals(AppointmentStatus.CONFIRMED, savedCaptor.getValue().getStatus());
        verify(notificationService).notifyStatusChange(savedCaptor.getValue(), AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    }
}
