package com.fyp.AABookingProject.announcement.service;

import com.fyp.AABookingProject.announcement.model.CreateAnnouncementRequest;
import com.fyp.AABookingProject.announcement.model.CreateAnnouncementResponse;
import com.fyp.AABookingProject.announcement.model.GetAnnouncementListResponse;
import com.fyp.AABookingProject.announcement.model.GetAnnouncementResponse;
import com.fyp.AABookingProject.announcement.repository.AnnouncementRepository;
import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Announcement;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {
    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdvisorRepository advisorRepository;

    public void createAnnouncement(CreateAnnouncementRequest createAnnouncementRequest){
        Date now = new Date();

//        Get username
        UserDetails userDetails = getUserDetails();
        Optional<User> userRepo =  userRepository.findByUsername(userDetails.getUsername());

//        Declare variable
        User user = new User();
        Announcement announcement = new Announcement();
        String fullName;

        if(userRepo.isPresent()){
            user.setFirstName(userRepo.get().getFirstName());
            user.setLastName(userRepo.get().getLastName());
            fullName = user.getFirstName() + " " + user.getLastName();

            announcement.setTitle(createAnnouncementRequest.getTitle());
            announcement.setContent(createAnnouncementRequest.getContent());
            announcement.setPublisherName(fullName);
            announcement.setCreatedAt(LocalDateTime.now());

            announcementRepository.save(announcement);
        } else {
            throw new IllegalArgumentException("Username not found.");
        }
    }

    public GetAnnouncementListResponse getAllAnnouncements(){
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        return (GetAnnouncementListResponse) announcements;
    }

    private UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (UserDetails) authentication.getPrincipal();
    }
}
