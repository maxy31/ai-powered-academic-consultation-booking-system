package com.fyp.AABookingProject.announcement.controller;

import com.fyp.AABookingProject.announcement.model.CreateAnnouncementRequest;
import com.fyp.AABookingProject.announcement.model.CreateAnnouncementResponse;
import com.fyp.AABookingProject.announcement.model.GetAnnouncementResponse;
import com.fyp.AABookingProject.announcement.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    @Autowired
    AnnouncementService announcementService;

    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        return ResponseEntity.ok("Announcement Test Successfully");
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
    @PostMapping("/postAnnouncement")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<CreateAnnouncementResponse> createAnnouncement(HttpServletRequest request, CreateAnnouncementRequest createAnnouncementRequest){
        announcementService.createAnnouncement(createAnnouncementRequest);

        CreateAnnouncementResponse response = new CreateAnnouncementResponse();
        response.setStatusMessage("Success");

        return ResponseEntity.ok(response);
    }

//    Get announcement
    @GetMapping("/getAnnouncement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVISOR') or hasRole('STUDENT')")
    public ResponseEntity<GetAnnouncementResponse> getAnnouncement(HttpServletRequest request){
        List<GetAnnouncementResponse> allAnnouncements;
    }
}
