package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.Notification;
import com.workflow.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMy(
            @AuthenticationPrincipal Employee employee) {
        List<NotificationResponse> list = notificationService.getNotifications(employee.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(employee.getId())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id,
                                                                          @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(
                toResponse(notificationService.markAsRead(id, employee.getId()))));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal Employee employee) {
        notificationService.markAllAsRead(employee.getId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
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
