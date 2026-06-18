package com.workflow.service;

import com.workflow.dto.NotificationResponse;
import com.workflow.entity.Employee;
import com.workflow.entity.Notification;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               EmployeeRepository employeeRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Notification> getNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getRecipient().getId().equals(recipientId))
            throw new RuntimeException("Notification does not belong to this user");
        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        broadcastNotification(recipientId, saved);
        broadcastUnreadCount(recipientId);
        return saved;
    }

    @Transactional
    public void markAllAsRead(Long recipientId) {
        notificationRepository.markAllAsReadByRecipientId(recipientId);
        broadcastUnreadCount(recipientId);
    }

    @Async
    public void broadcastNotification(Long recipientId, Notification notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, toResponse(notification));
        } catch (Exception e) {
            System.err.println("WebSocket notification broadcast failed: " + e.getMessage());
        }
    }

    @Async
    public void broadcastUnreadCount(Long recipientId) {
        try {
            long count = getUnreadCount(recipientId);
            messagingTemplate.convertAndSend("/topic/notifications/" + recipientId + "/count", count);
        } catch (Exception e) {
            System.err.println("WebSocket unread count broadcast failed: " + e.getMessage());
        }
    }

    @Async
    public void broadcastNotificationToRecipient(Notification notification) {
        try {
            Long recipientId = notification.getRecipient().getId();
            messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, toResponse(notification));
            broadcastUnreadCount(recipientId);
        } catch (Exception e) {
            System.err.println("WebSocket notification broadcast failed: " + e.getMessage());
        }
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getType());
        r.setTitle(n.getTitle());
        r.setBody(n.getBody());
        r.setIsRead(n.getIsRead() != null ? n.getIsRead() : false);
        r.setRelatedEntityType(n.getRelatedEntityType());
        r.setRelatedEntityId(n.getRelatedEntityId());
        r.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
        return r;
    }
}
