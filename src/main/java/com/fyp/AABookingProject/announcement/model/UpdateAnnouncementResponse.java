package com.fyp.AABookingProject.announcement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateAnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private String publisherName;

    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}
