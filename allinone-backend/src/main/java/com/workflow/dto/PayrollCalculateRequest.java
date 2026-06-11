package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PayrollCalculateRequest {
    @NotNull private Long employeeId;
    @NotBlank private String periodStart;
    @NotBlank private String periodEnd;

    public PayrollCalculateRequest() {}

    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getPeriodStart() { return periodStart; } public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }
    public String getPeriodEnd() { return periodEnd; } public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }
}
