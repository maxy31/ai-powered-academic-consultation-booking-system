package com.fyp.AABookingProject.ai.service;

import com.fyp.AABookingProject.ai.model.FreeSlot;
import com.fyp.AABookingProject.core.entity.*;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.appointment.repository.AppointmentRepository;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.ocr.repository.TimetableEntryRepository;
import com.fyp.AABookingProject.ocr.repository.TimetableRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AvailabilityServiceTest {

    @Mock TimetableRepository timetableRepository;
    @Mock TimetableEntryRepository timetableEntryRepository;
    @Mock UserRepository userRepository;
    @Mock AppointmentRepository appointmentRepository;

    AvailabilityService service;

    @BeforeEach
    void setup() {
        service = new AvailabilityService(timetableRepository, timetableEntryRepository, userRepository, appointmentRepository);
        // current user is student with advisor
        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("stu").password("x").authorities(List.of()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));

        User studentUser = new User(); studentUser.setId(1L); studentUser.setUsername("stu");
        Student s = new Student(); Advisor a = new Advisor(); User au = new User(); au.setId(2L); a.setUser(au); a.setId(20L); s.setAdvisor(a); studentUser.setStudent(s);
        when(userRepository.findByUsername("stu")).thenReturn(Optional.of(studentUser));

        // timetable for both users: Mon 09:00-12:00 busy (so free 08:00-09:00 and 12:00-18:00)
        Timetable tStu = new Timetable(); tStu.setId(100L); tStu.setUserId(1L);
        Timetable tAdv = new Timetable(); tAdv.setId(200L); tAdv.setUserId(2L);
        when(timetableRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(tStu));
        when(timetableRepository.findFirstByUserIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.of(tAdv));
        TimetableEntry e1 = new TimetableEntry(); e1.setTimetable(tStu); e1.setDay("Mon"); e1.setStartTime(LocalTime.of(9,0)); e1.setEndTime(LocalTime.of(12,0));
        TimetableEntry e2 = new TimetableEntry(); e2.setTimetable(tAdv); e2.setDay("Mon"); e2.setStartTime(LocalTime.of(9,0)); e2.setEndTime(LocalTime.of(12,0));
        when(timetableEntryRepository.findByTimetableId(100L)).thenReturn(List.of(e1));
        when(timetableEntryRepository.findByTimetableId(200L)).thenReturn(List.of(e2));

        // No existing appointments; the service will query a two-week window; simplify to return empty
        when(appointmentRepository.findByAdvisorIdAndDateBetween(eq(20L), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void recommend_shouldReturnNonEmptySlots_andRespectBookedExclusion() {
        List<FreeSlot> slots = service.recommend();
        assertNotNull(slots);
        assertTrue(slots.stream().anyMatch(fs -> fs.getDay().equals("Mon")));
        // Inject a booked appointment covering Mon 12:00-12:30 on the first Monday; then ensure slots would normally include it, but exclusion will remove it
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.minusDays((today.getDayOfWeek().getValue()+6)%7);
        Appointment booked = new Appointment();
        booked.setDate(thisMonday); booked.setStartTime(LocalTime.of(12,0)); booked.setEndTime(LocalTime.of(12,30));
        booked.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByAdvisorIdAndDateBetween(eq(20L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(booked));

        List<FreeSlot> slots2 = service.recommend();
        assertTrue(slots2.stream().noneMatch(fs -> fs.getDate().equals(thisMonday) && fs.getStartTime().equals("12:00") && fs.getEndTime().equals("12:30")));
    }
}
