package com.fyp.AABookingProject.announcement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAnnouncementListResponse {
    private List<GetAnnouncementResponse> announcementList;
}
