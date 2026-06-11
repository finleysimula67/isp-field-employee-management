package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "tasks")
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_by", nullable = false) private Employee assignedBy;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to", nullable = false) private Employee assignedTo;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) private TaskPriority priority;
    @Enumerated(EnumType.STRING) private TaskStatus status;
    @Column(name = "scheduled_date") private LocalDate scheduledDate;
    @Column(name = "customer_name") private String customerName;
    @Column(name = "customer_phone") private String customerPhone;
    @Column(name = "customer_address") private String customerAddress;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;

    public Task() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Employee getAssignedBy() { return assignedBy; } public void setAssignedBy(Employee assignedBy) { this.assignedBy = assignedBy; }
    public Employee getAssignedTo() { return assignedTo; } public void setAssignedTo(Employee assignedTo) { this.assignedTo = assignedTo; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public TaskPriority getPriority() { return priority; } public void setPriority(TaskPriority priority) { this.priority = priority; }
    public TaskStatus getStatus() { return status; } public void setStatus(TaskStatus status) { this.status = status; }
    public LocalDate getScheduledDate() { return scheduledDate; } public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; } public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TaskStatus.OPEN;
        if (priority == null) priority = TaskPriority.MEDIUM;
    }
}
