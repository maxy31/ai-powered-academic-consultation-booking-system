package com.fyp.AABookingProject.notification.model;

import lombok.Data;

import java.util.List;

@Data
public class NotificationBatchMarkReadRequest {
    private List<Long> ids;
}
