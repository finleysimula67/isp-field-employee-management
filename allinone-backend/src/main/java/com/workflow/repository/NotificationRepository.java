package com.workflow.repository;

import com.workflow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = ?1 AND (n.isRead IS NULL OR n.isRead = false)")
    void markAllAsReadByRecipientId(Long recipientId);
}
