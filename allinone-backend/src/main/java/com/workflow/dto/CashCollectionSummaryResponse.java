package com.workflow.dto;

import java.util.List;
import java.util.Map;

public class CashCollectionSummaryResponse {
    private Long employeeId;
    private String employeeName;
    private Map<Integer, List<DayCollectionEntry>> days;
    private java.math.BigDecimal totalCollected;
    private java.math.BigDecimal totalPending;
    private java.math.BigDecimal totalSubmitted;
    private java.math.BigDecimal totalRejected;
    private int approvedCount;
    private int pendingCount;

    public CashCollectionSummaryResponse() {}

    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Map<Integer, List<DayCollectionEntry>> getDays() { return days; } public void setDays(Map<Integer, List<DayCollectionEntry>> days) { this.days = days; }
    public java.math.BigDecimal getTotalCollected() { return totalCollected; } public void setTotalCollected(java.math.BigDecimal totalCollected) { this.totalCollected = totalCollected; }
    public java.math.BigDecimal getTotalPending() { return totalPending; } public void setTotalPending(java.math.BigDecimal totalPending) { this.totalPending = totalPending; }
    public java.math.BigDecimal getTotalSubmitted() { return totalSubmitted; } public void setTotalSubmitted(java.math.BigDecimal totalSubmitted) { this.totalSubmitted = totalSubmitted; }
    public java.math.BigDecimal getTotalRejected() { return totalRejected; } public void setTotalRejected(java.math.BigDecimal totalRejected) { this.totalRejected = totalRejected; }
    public int getApprovedCount() { return approvedCount; } public void setApprovedCount(int approvedCount) { this.approvedCount = approvedCount; }
    public int getPendingCount() { return pendingCount; } public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
}
