package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportRequest {
    @NotBlank private String startDate;
    @NotBlank private String endDate;
    private Long employeeId; private Long branchId;
    @NotBlank private String format;

    public ReportRequest() {}

    public String getStartDate() { return startDate; } public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; } public void setEndDate(String endDate) { this.endDate = endDate; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getFormat() { return format; } public void setFormat(String format) { this.format = format; }
}
