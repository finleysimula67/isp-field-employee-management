package com.workflow.dto;

public class LeaveRequestResponse {
    private Long id; private Long employeeId; private String employeeName;
    private String leaveType; private String startDate; private String endDate;
    private int durationDays; private String reason; private String status;
    private Long reviewedBy; private String reviewedByName; private String reviewComment;
    private boolean deductedFromBalance; private String submittedAt; private String reviewedAt;

    public LeaveRequestResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getLeaveType() { return leaveType; } public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getStartDate() { return startDate; } public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; } public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getDurationDays() { return durationDays; } public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getReviewedBy() { return reviewedBy; } public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewedByName() { return reviewedByName; } public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public boolean isDeductedFromBalance() { return deductedFromBalance; }
    public void setDeductedFromBalance(boolean deductedFromBalance) { this.deductedFromBalance = deductedFromBalance; }
    public String getSubmittedAt() { return submittedAt; } public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; } public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
}
