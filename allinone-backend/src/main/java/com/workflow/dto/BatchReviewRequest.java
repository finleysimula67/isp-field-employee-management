package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BatchReviewRequest {
    @NotEmpty private List<Long> ids;
    @NotBlank private String status;
    private String reviewComment;

    public BatchReviewRequest() {}

    public List<Long> getIds() { return ids; } public void setIds(List<Long> ids) { this.ids = ids; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
