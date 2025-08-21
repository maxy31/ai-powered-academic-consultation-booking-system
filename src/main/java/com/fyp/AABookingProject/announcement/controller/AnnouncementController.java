package com.fyp.AABookingProject.announcement.controller;

import com.fyp.AABookingProject.announcement.model.*;
import com.fyp.AABookingProject.announcement.service.AnnouncementService;
import com.fyp.AABookingProject.core.entity.Announcement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    @Autowired
    AnnouncementService announcementService;

    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        return ResponseEntity.ok("Welcome to Announcement.");
    }

    @GetMapping("/testStudent")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> test2(HttpServletRequest request) {
        return ResponseEntity.ok("Announcement Test Student Successfully");
    }

    @GetMapping("/testAdvisor")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<String> test3(HttpServletRequest request) {
        return ResponseEntity.ok("Announcement Test Advisor Successfully");
    }

//    Post announcement
    @PostMapping("/createAnnouncement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVISOR')")
    public ResponseEntity<CreateAnnouncementResponse> createAnnouncement(@Valid @RequestBody CreateAnnouncementRequest createAnnouncementRequest, HttpServletRequest request){
        CreateAnnouncementResponse response = announcementService.createAnnouncement(createAnnouncementRequest);
        return ResponseEntity.ok(response);
    }

//    Get announcement
    @GetMapping("/getAnnouncement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVISOR') or hasRole('STUDENT')")
    public ResponseEntity<GetAnnouncementResponse> getAnnouncement(HttpServletRequest request){
        List<Announcement> allAnnouncements = announcementService.getAllAnnouncements();
        GetAnnouncementResponse getAnnouncementResponse = new GetAnnouncementResponse(allAnnouncements);
        return ResponseEntity.ok(getAnnouncementResponse);
    }

//    Update announcement
    @PostMapping("/updateAnnouncement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVISOR')")
    public ResponseEntity<UpdateAnnouncementResponse> updateAnnouncement(@Valid @RequestBody UpdateAnnouncementRequest updateAnnouncementRequest, HttpServletRequest request){
        UpdateAnnouncementResponse updateAnnouncementResponse = announcementService.updateAnnouncement(updateAnnouncementRequest);
        return ResponseEntity.ok(updateAnnouncementResponse);
    }

//    Delete announcement
    @PostMapping("/deleteAnnouncement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVISOR')")
    public ResponseEntity<DeleteAnnouncementResponse> deleteAnnouncement(@Valid @RequestBody DeleteAnnouncementRequest deleteAnnouncementRequest, HttpServletRequest request){
        DeleteAnnouncementResponse deleteAnnouncementResponse = announcementService.deleteAnnouncement(deleteAnnouncementRequest);
        return ResponseEntity.ok(deleteAnnouncementResponse);
    }
}
