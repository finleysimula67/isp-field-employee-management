package com.workflow.dto;

public class TaskResponse {
    private Long id; private Long assignedBy; private String assignedByName;
    private Long assignedTo; private String assignedToName; private String title;
    private String description; private String priority; private String status;
    private String scheduledDate; private String customerName;
    private String customerPhone; private String customerAddress;
    private String createdAt; private String completedAt;

    public TaskResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getAssignedBy() { return assignedBy; } public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }
    public String getAssignedByName() { return assignedByName; } public void setAssignedByName(String assignedByName) { this.assignedByName = assignedByName; }
    public Long getAssignedTo() { return assignedTo; } public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }
    public String getAssignedToName() { return assignedToName; } public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; } public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getScheduledDate() { return scheduledDate; } public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCompletedAt() { return completedAt; } public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}
