package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "notifications")
public class Notification implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipient_id", nullable = false) private Employee recipient;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String body;
    @Column(name = "is_read") private Boolean isRead;
    @Column(name = "related_entity_type") private String relatedEntityType;
    @Column(name = "related_entity_id") private Long relatedEntityId;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    public Notification() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Employee getRecipient() { return recipient; } public void setRecipient(Employee recipient) { this.recipient = recipient; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; } public void setBody(String body) { this.body = body; }
    public Boolean getIsRead() { return isRead; } public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
    }
}
