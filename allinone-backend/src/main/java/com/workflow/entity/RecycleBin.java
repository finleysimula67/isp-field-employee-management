package com.workflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recycle_bin")
public class RecycleBin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "entity_type", nullable = false) private String entityType;
    @Column(name = "entity_id", nullable = false) private Long entityId;
    @Column(name = "record_data", columnDefinition = "TEXT") private String recordData;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "deleted_by_id") private Employee deletedBy;
    @Column(name = "deleted_at", nullable = false) private LocalDateTime deletedAt;
    @Column(name = "original_created_at") private LocalDateTime originalCreatedAt;
    @Column(name = "original_owner_id") private Long originalOwnerId;
    @Column(name = "restored_at") private LocalDateTime restoredAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "restored_by_id") private Employee restoredBy;

    public RecycleBin() {}

    @PrePersist
    protected void onCreate() { if (deletedAt == null) deletedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEntityType() { return entityType; } public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; } public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getRecordData() { return recordData; } public void setRecordData(String recordData) { this.recordData = recordData; }
    public Employee getDeletedBy() { return deletedBy; } public void setDeletedBy(Employee deletedBy) { this.deletedBy = deletedBy; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getOriginalCreatedAt() { return originalCreatedAt; } public void setOriginalCreatedAt(LocalDateTime originalCreatedAt) { this.originalCreatedAt = originalCreatedAt; }
    public Long getOriginalOwnerId() { return originalOwnerId; } public void setOriginalOwnerId(Long originalOwnerId) { this.originalOwnerId = originalOwnerId; }
    public LocalDateTime getRestoredAt() { return restoredAt; } public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
    public Employee getRestoredBy() { return restoredBy; } public void setRestoredBy(Employee restoredBy) { this.restoredBy = restoredBy; }
}
