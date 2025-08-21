package com.fyp.AABookingProject.announcement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeleteAnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private String publisherName;
}
