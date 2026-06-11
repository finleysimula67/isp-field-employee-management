package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "entity_type", nullable = false) private String entityType;
    @Column(name = "entity_id", nullable = false) private Long entityId;
    @Column(nullable = false) private String action;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_id") private Employee actor;
    @Column(name = "previous_status") private String previousStatus;
    @Column(name = "new_status") private String newStatus;
    @Column(columnDefinition = "TEXT") private String metadata;
    @Column(name = "ip_address") private String ipAddress;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;

    public AuditLog() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEntityType() { return entityType; } public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; } public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public Employee getActor() { return actor; } public void setActor(Employee actor) { this.actor = actor; }
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; } public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getMetadata() { return metadata; } public void setMetadata(String metadata) { this.metadata = metadata; }
    public String getIpAddress() { return ipAddress; } public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
