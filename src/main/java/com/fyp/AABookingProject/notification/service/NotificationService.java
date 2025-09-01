package com.fyp.AABookingProject.notification.service;

import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.StudentRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.notification.entity.Notification;
import com.fyp.AABookingProject.notification.enumClass.NotificationType;
import com.fyp.AABookingProject.notification.model.NotificationResponse;
import com.fyp.AABookingProject.notification.repository.NotificationRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AdvisorRepository advisorRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    private final NotificationPublisher notificationPublisher;

    public NotificationService(NotificationRepository notificationRepository,
                               AdvisorRepository advisorRepository,
                               StudentRepository studentRepository,
                               UserRepository userRepository,
                               NotificationPublisher notificationPublisher) {
        this.notificationRepository = notificationRepository;
        this.advisorRepository = advisorRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.notificationPublisher = notificationPublisher;
    }

    // Event hooks
    public void notifyAppointmentCreated(Appointment appt) {
        Advisor advisor = advisorRepository.findById(appt.getAdvisorId()).orElse(null);
        if (advisor == null) return;
        Long advisorUserId = advisor.getUser().getId();
        save(advisorUserId,
                NotificationType.APPOINTMENT_CREATED,
                "New Appointment Request",
                String.format("Student #%d requested %s %s-%s", appt.getStudentId(), appt.getDate(), appt.getStartTime(), appt.getEndTime()),
                appt.getId());
    }

    public void notifyStatusChange(Appointment appt, AppointmentStatus oldStatus, AppointmentStatus newStatus) {
        Student student = studentRepository.findById(appt.getStudentId()).orElse(null);
        Advisor advisor = advisorRepository.findById(appt.getAdvisorId()).orElse(null);
        String baseWindow = String.format("%s %s-%s", appt.getDate(), appt.getStartTime(), appt.getEndTime());
        // Student-facing message
        if (student != null) {
            Long studentUserId = student.getUser().getId();
            switch (newStatus) {
                case CONFIRMED -> save(studentUserId, NotificationType.APPOINTMENT_CONFIRMED, "Appointment Confirmed", "Your appointment on " + baseWindow + " was confirmed.", appt.getId());
                case REJECTED -> save(studentUserId, NotificationType.APPOINTMENT_REJECTED, "Appointment Rejected", "Your appointment on " + baseWindow + " was rejected.", appt.getId());
                case CANCELLED -> save(studentUserId, NotificationType.APPOINTMENT_CANCELLED, "Appointment Cancelled", "The appointment on " + baseWindow + " was cancelled.", appt.getId());
                default -> save(studentUserId, NotificationType.APPOINTMENT_UPDATED, "Appointment Updated", "Status changed to " + newStatus + " for " + baseWindow + ".", appt.getId());
            }
        }
        // Advisor-facing message
        if (advisor != null) {
            Long advisorUserId = advisor.getUser().getId();
            switch (newStatus) {
                case CONFIRMED -> save(advisorUserId, NotificationType.APPOINTMENT_CONFIRMED, "Appointment Confirmed", "You confirmed appointment " + baseWindow + ".", appt.getId());
                case REJECTED -> save(advisorUserId, NotificationType.APPOINTMENT_REJECTED, "Appointment Rejected", "You rejected appointment " + baseWindow + ".", appt.getId());
                case CANCELLED -> save(advisorUserId, NotificationType.APPOINTMENT_CANCELLED, "Appointment Cancelled", "Appointment " + baseWindow + " has been cancelled.", appt.getId());
                default -> save(advisorUserId, NotificationType.APPOINTMENT_UPDATED, "Appointment Updated", "Status changed to " + newStatus + " for " + baseWindow + ".", appt.getId());
            }
        }
    }

    public void notifyAppointmentTimeUpdated(Appointment appt, LocalDate oldDate, java.time.LocalTime oldStart, java.time.LocalTime oldEnd) {
        if (oldDate == null || oldStart == null || oldEnd == null) return;
        String oldWindow = String.format("%s %s-%s", oldDate, oldStart, oldEnd);
        String newWindow = String.format("%s %s-%s", appt.getDate(), appt.getStartTime(), appt.getEndTime());
        Student student = studentRepository.findById(appt.getStudentId()).orElse(null);
        Advisor advisor = advisorRepository.findById(appt.getAdvisorId()).orElse(null);
        String title = "Appointment Updated";
        String msg = "Changed from " + oldWindow + " to " + newWindow;
        if (student != null) {
            save(student.getUser().getId(), NotificationType.APPOINTMENT_UPDATED, title, msg, appt.getId());
        }
        if (advisor != null) {
            save(advisor.getUser().getId(), NotificationType.APPOINTMENT_UPDATED, title, msg, appt.getId());
        }
    }

    public void notifyReminder(Appointment appt) {
        // Reminder to both advisor and student (only if confirmed)
        if (appt.getStatus() != AppointmentStatus.CONFIRMED) return;
        Advisor advisor = advisorRepository.findById(appt.getAdvisorId()).orElse(null);
        Student student = studentRepository.findById(appt.getStudentId()).orElse(null);
        String title = "Upcoming Appointment";
        String msg = String.format("Appointment starts at %s (%s-%s)", appt.getDate(), appt.getStartTime(), appt.getEndTime());
        if (advisor != null) {
            Long uid = advisor.getUser().getId();
            if (!notificationRepository.existsByRelatedAppointmentIdAndTypeAndRecipientUserId(appt.getId(), NotificationType.APPOINTMENT_REMINDER, uid)) {
                save(uid, NotificationType.APPOINTMENT_REMINDER, title, msg, appt.getId());
            }
        }
        if (student != null) {
            Long uid = student.getUser().getId();
            if (!notificationRepository.existsByRelatedAppointmentIdAndTypeAndRecipientUserId(appt.getId(), NotificationType.APPOINTMENT_REMINDER, uid)) {
                save(uid, NotificationType.APPOINTMENT_REMINDER, title, msg, appt.getId());
            }
        }
    }

    public List<NotificationResponse> list(boolean unreadOnly) {
        Long currentUserId = getCurrentUserEntityId();
    List<Notification> list = unreadOnly ?
        notificationRepository.findByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(currentUserId) :
        notificationRepository.findByRecipientUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(currentUserId);
        return list.stream().map(this::toResponse).toList();
    }

    public Page<NotificationResponse> listPaged(boolean unreadOnly, int page, int size) {
        Long currentUserId = getCurrentUserEntityId();
        Pageable pageable = PageRequest.of(page, size);
    Page<Notification> p = unreadOnly ?
        notificationRepository.findByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNull(currentUserId, pageable) :
        notificationRepository.findByRecipientUserIdAndDeletedAtIsNull(currentUserId, pageable);
        return p.map(this::toResponse);
    }

    public long unreadCount() {
        Long currentUserId = getCurrentUserEntityId();
    return notificationRepository.countByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNull(currentUserId);
    }

    public List<NotificationResponse> markReadBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        Long currentUser = getCurrentUserEntityId();
        List<NotificationResponse> responses = new ArrayList<>();
        ids.forEach(id -> {
            Notification n = notificationRepository.findById(id).orElse(null);
            if (n != null && n.getDeletedAt() == null && n.getRecipientUserId().equals(currentUser) && n.getReadAt() == null) {
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
            }
            if (n != null && n.getDeletedAt() == null && n.getRecipientUserId().equals(currentUser)) responses.add(toResponse(n));
        });
        return responses;
    }

    public long markAllRead() {
        Long currentUserId = getCurrentUserEntityId();
    List<Notification> list = notificationRepository.findByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(currentUserId);
        list.forEach(n -> { n.setReadAt(LocalDateTime.now()); });
        notificationRepository.saveAll(list);
        return list.size();
    }

    public NotificationResponse markRead(Long id) {
        Long currentUser = getCurrentUserEntityId();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getRecipientUserId().equals(currentUser) || n.getDeletedAt() != null) {
            throw new IllegalArgumentException("Forbidden");
        }
        if (n.getReadAt() == null) {
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
        return toResponse(n);
    }

    public NotificationResponse softDelete(Long id) {
        Long currentUser = getCurrentUserEntityId();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getRecipientUserId().equals(currentUser)) {
            throw new IllegalArgumentException("Forbidden");
        }
        if (n.getDeletedAt() == null) {
            n.setDeletedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
        return toResponse(n);
    }

    public int softDeleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        Long currentUser = getCurrentUserEntityId();
        int[] counter = {0};
        ids.forEach(id -> {
            Notification n = notificationRepository.findById(id).orElse(null);
            if (n != null && n.getRecipientUserId().equals(currentUser) && n.getDeletedAt() == null) {
                n.setDeletedAt(LocalDateTime.now());
                notificationRepository.save(n);
                counter[0]++;
            }
        });
        return counter[0];
    }

    private void save(Long recipientUserId, NotificationType type, String title, String message, Long apptId) {
        Notification n = Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .title(title)
                .message(message)
                .relatedAppointmentId(apptId)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(n);
        notificationPublisher.publishToUser(recipientUserId, toResponse(n));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedAppointmentId(n.getRelatedAppointmentId())
                .createdAt(n.getCreatedAt())
                .read(n.isRead())
                .build();
    }

    private Long getCurrentUserEntityId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        UserDetails ud = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
    }
}
