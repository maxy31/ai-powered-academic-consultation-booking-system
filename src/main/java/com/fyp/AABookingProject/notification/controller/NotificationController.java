package com.fyp.AABookingProject.notification.controller;

import com.fyp.AABookingProject.notification.model.NotificationResponse;
import com.fyp.AABookingProject.notification.model.NotificationBatchMarkReadRequest;
import com.fyp.AABookingProject.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "unreadOnly", required = false, defaultValue = "false") boolean unreadOnly,
                                  @RequestParam(name = "page", required = false) Integer page,
                                  @RequestParam(name = "size", required = false) Integer size) {
        if (page != null && size != null) {
            return ResponseEntity.ok(notificationService.listPaged(unreadOnly, Math.max(page,0), Math.max(size,1)));
        }
        return ResponseEntity.ok(notificationService.list(unreadOnly));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @PostMapping("/mark-read-batch")
    public ResponseEntity<List<NotificationResponse>> markReadBatch(@RequestBody NotificationBatchMarkReadRequest req) {
        return ResponseEntity.ok(notificationService.markReadBatch(req.getIds()));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Long> markAllRead() {
        return ResponseEntity.ok(notificationService.markAllRead());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(notificationService.unreadCount());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<NotificationResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.softDelete(id));
    }

    @PostMapping("/delete-batch")
    public ResponseEntity<Integer> deleteBatch(@RequestBody NotificationBatchMarkReadRequest req) {
        return ResponseEntity.ok(notificationService.softDeleteBatch(req.getIds()));
    }
}
