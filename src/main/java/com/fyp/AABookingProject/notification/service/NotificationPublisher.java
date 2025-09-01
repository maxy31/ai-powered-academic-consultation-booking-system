package com.fyp.AABookingProject.notification.service;

import com.fyp.AABookingProject.notification.model.NotificationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fyp.AABookingProject.core.repository.UserRepository;

@Component
public class NotificationPublisher {
    private final SimpMessagingTemplate template;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);

    public NotificationPublisher(SimpMessagingTemplate template, UserRepository userRepository) {
        this.template = template;
        this.userRepository = userRepository;
    }

    public void publishToUser(Long userId, NotificationResponse payload) {
        try {
            String username = userRepository.findById(userId).map(u -> u.getUsername()).orElse("<unknown>");
            // 双发送用于调试映射问题
            template.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", payload);
            template.convertAndSendToUser(username, "/queue/notifications", payload);
            log.debug("Publish notif id={} type={} via keys [userId='{}', username='{}']", payload.getId(), payload.getType(), userId, username);
        } catch (Exception e) {
            log.warn("Publish notif failed userId={} err={}", userId, e.getMessage());
        }
    }
}
