package com.workflow.repository;

import com.workflow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.recipient WHERE n.recipient.id = :recipientId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") Long recipientId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND (n.isRead IS NULL OR n.isRead = false) AND n.deletedAt IS NULL")
    long countByRecipientIdAndIsReadFalse(@Param("recipientId") Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = ?1 AND (n.isRead IS NULL OR n.isRead = false)")
    void markAllAsReadByRecipientId(Long recipientId);

    @Modifying
    @Query(value = "UPDATE notifications SET recipient_id = :targetId WHERE recipient_id = :sourceId", nativeQuery = true)
    int transferRecipientId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
