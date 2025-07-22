package com.fyp.AABookingProject.announcement.service;

import com.fyp.AABookingProject.announcement.model.CreateAnnouncementRequest;
import com.fyp.AABookingProject.announcement.model.CreateAnnouncementResponse;
import com.fyp.AABookingProject.announcement.model.GetAnnouncementResponse;
import com.fyp.AABookingProject.announcement.repository.AnnouncementRepository;
import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Announcement;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {
    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    AdvisorRepository advisorRepository;

    public void createAnnouncement(CreateAnnouncementRequest createAnnouncementRequest){
        Date now = new Date();
        Advisor advisor = advisorRepository.findById(createAnnouncementRequest.getAdvisorId())
                .orElseThrow(() -> new RuntimeException("Advisor not found"));

        Announcement announcement = new Announcement();
        announcement.setTitle(createAnnouncementRequest.getTitle());
        announcement.setContent(createAnnouncementRequest.getContent());
        announcement.setAdvisor(advisor);
        announcement.setCreatedAt(LocalDateTime.now());

        announcementRepository.save(announcement);
    }

    public List<GetAnnouncementResponse> getAllAnnouncements(){
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        return announcements.stream()
                .map(Announcement::fromEntity)
                .collect(Collectors.toList());
    }
}
