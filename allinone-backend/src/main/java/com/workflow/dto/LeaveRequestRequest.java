package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class LeaveRequestRequest {
    @NotBlank private String leaveType;
    @NotBlank private String startDate;
    @NotBlank private String endDate;
    private String reason;

    public LeaveRequestRequest() {}

    public String getLeaveType() { return leaveType; } public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getStartDate() { return startDate; } public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; } public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
}
