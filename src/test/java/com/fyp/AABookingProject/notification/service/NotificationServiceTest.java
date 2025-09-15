package com.fyp.AABookingProject.notification.service;

import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.StudentRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.notification.entity.Notification;
import com.fyp.AABookingProject.notification.repository.NotificationRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock AdvisorRepository advisorRepository;
    @Mock StudentRepository studentRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationPublisher publisher;

    @InjectMocks NotificationService service;

    @BeforeEach
    void setupAuth() {
        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("u1").password("x").authorities(List.of()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));
        User u = new User(); u.setId(100L); u.setUsername("u1");
    lenient().when(userRepository.findByUsername("u1")).thenReturn(Optional.of(u));
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void notifyStatusChange_shouldSaveForStudentAndAdvisor() {
        // Given
        Appointment appt = new Appointment();
        appt.setId(1L); appt.setStudentId(10L); appt.setAdvisorId(20L);
        appt.setDate(LocalDate.of(2099,1,1));
        appt.setStartTime(LocalTime.of(9,0)); appt.setEndTime(LocalTime.of(9,30));

        Student stu = new Student(); User su = new User(); su.setId(200L); stu.setUser(su);
        Advisor adv = new Advisor(); User au = new User(); au.setId(300L); adv.setUser(au);
        when(studentRepository.findById(10L)).thenReturn(Optional.of(stu));
        when(advisorRepository.findById(20L)).thenReturn(Optional.of(adv));

        // When
        service.notifyStatusChange(appt, AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

        // Then: two notifications persisted and published
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeast(2)).save(captor.capture());
        List<Notification> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(n -> n.getRecipientUserId().equals(200L)));
        assertTrue(saved.stream().anyMatch(n -> n.getRecipientUserId().equals(300L)));
        verify(publisher, atLeast(2)).publishToUser(anyLong(), any());
    }

    @Test
    void markRead_shouldThrow_whenNotOwner() {
        // Given notification belongs to another user
        Notification n = Notification.builder().id(5L).recipientUserId(999L).build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.markRead(5L));
        assertTrue(ex.getMessage().toLowerCase().contains("forbidden"));
        verify(notificationRepository, never()).save(any());
    }
}
