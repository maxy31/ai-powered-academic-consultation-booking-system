package com.fyp.AABookingProject.announcement.service;

import com.fyp.AABookingProject.announcement.model.*;
import com.fyp.AABookingProject.announcement.repository.AnnouncementRepository;
import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Announcement;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
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

//    Create announcement
    public CreateAnnouncementResponse createAnnouncement(CreateAnnouncementRequest createAnnouncementRequest){
//        Get username
        UserDetails userDetails = getUserDetails();
        Optional<User> userTarget =  userRepository.findByUsername(userDetails.getUsername());

//        Declare variable
        User user = new User();
        String fullName = "unknown";
        Announcement announcement = new Announcement();
        CreateAnnouncementResponse response = new CreateAnnouncementResponse();

        if(userTarget.isPresent()){
            user = userTarget.get();
            fullName = user.getFirstName() + " " + user.getLastName();

            announcement.setTitle(createAnnouncementRequest.getTitle());
            announcement.setContent(createAnnouncementRequest.getContent());
            announcement.setPublisherName(fullName);
            announcement.setCreatedAt(LocalDateTime.now());

            announcementRepository.save(announcement);
        } else {
            throw new IllegalArgumentException("Username not found.");
        }

        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setPublisherName(announcement.getPublisherName());
        response.setCreatedAt(announcement.getCreatedAt());

        return response;
    }

//    Get announcement in list
    public List<Announcement> getAllAnnouncements(){
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

//    Update announcement
    public UpdateAnnouncementResponse updateAnnouncement(UpdateAnnouncementRequest updateAnnouncementRequest){
        Optional<Announcement> announcementTarget = announcementRepository.findById(updateAnnouncementRequest.getId());
        Announcement announcement = new Announcement();
        UpdateAnnouncementResponse response = new UpdateAnnouncementResponse();

        if(announcementTarget.isPresent()){
            announcement = announcementTarget.get();
            announcement.setTitle(updateAnnouncementRequest.getTitle());
            announcement.setContent(updateAnnouncementRequest.getContent());
            announcement.setUpdatedAt(LocalDateTime.now());

            announcementRepository.save(announcement);
        } else {
            throw new RuntimeException("Announcement Not Found.");
        }

        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setPublisherName(announcement.getPublisherName());
        response.setUpdatedAt(announcement.getUpdatedAt());
        return response;
    }

    public DeleteAnnouncementResponse deleteAnnouncement(DeleteAnnouncementRequest deleteAnnouncementRequest){
        Optional<Announcement> announcementTarget = announcementRepository.findById(deleteAnnouncementRequest.getId());
        Announcement announcement = new Announcement();
        DeleteAnnouncementResponse deleteAnnouncementResponse = new DeleteAnnouncementResponse();

        if(announcementTarget.isPresent()){
            announcement = announcementTarget.get();

            deleteAnnouncementResponse.setId(announcement.getId());
            deleteAnnouncementResponse.setTitle(announcement.getTitle());
            deleteAnnouncementResponse.setContent(announcement.getContent());
            deleteAnnouncementResponse.setPublisherName(announcement.getPublisherName());

            announcementRepository.delete(announcement);
        } else {
            throw new RuntimeException("Announcement Not Found.");
        }
        return deleteAnnouncementResponse;
    }

    private UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (UserDetails) authentication.getPrincipal();
    }
}
