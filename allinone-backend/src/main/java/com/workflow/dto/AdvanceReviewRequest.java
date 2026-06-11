package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class AdvanceReviewRequest {
    @NotBlank private String status;
    private String notes;

    public AdvanceReviewRequest() {}

    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
}
