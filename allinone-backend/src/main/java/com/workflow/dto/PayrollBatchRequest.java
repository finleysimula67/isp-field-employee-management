package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class PayrollBatchRequest {
    @NotBlank private String periodStart;
    @NotBlank private String periodEnd;
    private List<Long> employeeIds;

    public PayrollBatchRequest() {}

    public String getPeriodStart() { return periodStart; } public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }
    public String getPeriodEnd() { return periodEnd; } public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }
    public List<Long> getEmployeeIds() { return employeeIds; } public void setEmployeeIds(List<Long> employeeIds) { this.employeeIds = employeeIds; }
}
