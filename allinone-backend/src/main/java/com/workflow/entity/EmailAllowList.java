package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "email_allow_list")
public class EmailAllowList implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String email;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "added_by") private Employee addedBy;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    public EmailAllowList() {}
    public EmailAllowList(String email) { this.email = email; }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public Employee getAddedBy() { return addedBy; } public void setAddedBy(Employee addedBy) { this.addedBy = addedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; } public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
