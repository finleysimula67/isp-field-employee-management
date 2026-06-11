package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "email_allow_list")
public class EmailAllowList {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String email;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "added_by") private Employee addedBy;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;

    public EmailAllowList() {}
    public EmailAllowList(String email) { this.email = email; }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public Employee getAddedBy() { return addedBy; } public void setAddedBy(Employee addedBy) { this.addedBy = addedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
