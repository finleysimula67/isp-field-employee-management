package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequest {
    @NotNull private Long assignedTo;
    @NotBlank private String title;
    private String description; private String priority;
    private String scheduledDate; private String customerName;
    private String customerPhone; private String customerAddress;

    public TaskRequest() {}

    public Long getAssignedTo() { return assignedTo; } public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; } public void setPriority(String priority) { this.priority = priority; }
    public String getScheduledDate() { return scheduledDate; } public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
}
