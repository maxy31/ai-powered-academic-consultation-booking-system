package com.fyp.AABookingProject.notification.repository;

import com.fyp.AABookingProject.notification.entity.Notification;
import com.fyp.AABookingProject.notification.enumClass.NotificationType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);
    List<Notification> findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(Long recipientUserId);
    Page<Notification> findByRecipientUserId(Long recipientUserId, Pageable pageable);
    Page<Notification> findByRecipientUserIdAndReadAtIsNull(Long recipientUserId, Pageable pageable);
    boolean existsByRelatedAppointmentIdAndTypeAndRecipientUserId(Long relatedAppointmentId, NotificationType type, Long recipientUserId);
    long countByRecipientUserIdAndReadAtIsNull(Long recipientUserId);
    // Excluding soft-deleted
    List<Notification> findByRecipientUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long recipientUserId);
    List<Notification> findByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(Long recipientUserId);
    Page<Notification> findByRecipientUserIdAndDeletedAtIsNull(Long recipientUserId, Pageable pageable);
    Page<Notification> findByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNull(Long recipientUserId, Pageable pageable);
    long countByRecipientUserIdAndReadAtIsNullAndDeletedAtIsNull(Long recipientUserId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.deletedAt = CURRENT_TIMESTAMP WHERE n.readAt IS NOT NULL AND n.deletedAt IS NULL AND n.createdAt < :threshold")
    int softDeleteReadBefore(java.time.LocalDateTime threshold);

    @Transactional
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.deletedAt IS NOT NULL AND n.deletedAt < :threshold")
    int purgeDeletedBefore(java.time.LocalDateTime threshold);
}
