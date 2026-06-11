package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class LeaveReviewRequest {
    @NotBlank private String status;
    private String reviewComment;

    public LeaveReviewRequest() {}

    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
