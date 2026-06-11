package com.workflow.dto;

public class SalaryAdvanceResponse {
    private Long id; private Long employeeId; private String employeeName;
    private Double amount; private String requestDate; private String reason;
    private String status; private Long approvedBy; private String approvedByName;
    private String approvedAt; private String disbursedAt; private boolean isSettled;
    private Long settledInPayrollId; private String notes;

    public SalaryAdvanceResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Double getAmount() { return amount; } public void setAmount(Double amount) { this.amount = amount; }
    public String getRequestDate() { return requestDate; } public void setRequestDate(String requestDate) { this.requestDate = requestDate; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getApprovedBy() { return approvedBy; } public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public String getApprovedByName() { return approvedByName; } public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
    public String getApprovedAt() { return approvedAt; } public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
    public String getDisbursedAt() { return disbursedAt; } public void setDisbursedAt(String disbursedAt) { this.disbursedAt = disbursedAt; }
    public boolean getIsSettled() { return isSettled; } public void setIsSettled(boolean isSettled) { this.isSettled = isSettled; }
    public Long getSettledInPayrollId() { return settledInPayrollId; }
    public void setSettledInPayrollId(Long settledInPayrollId) { this.settledInPayrollId = settledInPayrollId; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
}
