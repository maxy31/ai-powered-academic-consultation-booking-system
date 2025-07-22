package com.fyp.AABookingProject.announcement.model;

import com.fyp.AABookingProject.core.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
