package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskStatusUpdateRequest {
    @NotBlank private String status;

    public TaskStatusUpdateRequest() {}

    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
}
