package com.fyp.AABookingProject.notification.controller;

import com.fyp.AABookingProject.notification.model.NotificationResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private static final Map<Long, SseEmitter> EMITTERS = new ConcurrentHashMap<>();
    private final com.fyp.AABookingProject.core.repository.UserRepository userRepository;

    public NotificationSseController(com.fyp.AABookingProject.core.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream() {
        Long userId = currentUserId();
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        EMITTERS.put(userId, emitter);
        emitter.onTimeout(() -> EMITTERS.remove(userId));
        emitter.onCompletion(() -> EMITTERS.remove(userId));
        try { emitter.send(SseEmitter.event().name("init").data("connected")); } catch (IOException ignored) {}
        return ResponseEntity.ok(emitter);
    }

    // Static helper for publisher bridging (optional future use)
    public static void push(Long userId, NotificationResponse resp) {
        SseEmitter emitter = EMITTERS.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(resp));
            } catch (IOException e) {
                EMITTERS.remove(userId);
            }
        }
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        UserDetails ud = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
    }
}
