package com.workflow.dto;

import java.util.List;
import java.util.Map;

public class CashCollectionSummaryResponse {
    private Long employeeId;
    private String employeeName;
    private Map<Integer, List<DayCollectionEntry>> days;
    private Double totalCollected;
    private Double totalPending;
    private Double totalSubmitted;
    private Double totalRejected;
    private int approvedCount;
    private int pendingCount;

    public CashCollectionSummaryResponse() {}

    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Map<Integer, List<DayCollectionEntry>> getDays() { return days; } public void setDays(Map<Integer, List<DayCollectionEntry>> days) { this.days = days; }
    public Double getTotalCollected() { return totalCollected; } public void setTotalCollected(Double totalCollected) { this.totalCollected = totalCollected; }
    public Double getTotalPending() { return totalPending; } public void setTotalPending(Double totalPending) { this.totalPending = totalPending; }
    public Double getTotalSubmitted() { return totalSubmitted; } public void setTotalSubmitted(Double totalSubmitted) { this.totalSubmitted = totalSubmitted; }
    public Double getTotalRejected() { return totalRejected; } public void setTotalRejected(Double totalRejected) { this.totalRejected = totalRejected; }
    public int getApprovedCount() { return approvedCount; } public void setApprovedCount(int approvedCount) { this.approvedCount = approvedCount; }
    public int getPendingCount() { return pendingCount; } public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
}
