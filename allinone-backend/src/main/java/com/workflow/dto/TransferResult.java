package com.workflow.dto;

import java.util.Map;

public class TransferResult {
    private Long sourceEmployeeId;
    private String sourceEmployeeName;
    private Long targetEmployeeId;
    private String targetEmployeeName;
    private Map<String, Integer> transferredCounts;
    private boolean sourceDeleted;
    private int totalTransferred;

    public TransferResult() {}

    public Long getSourceEmployeeId() { return sourceEmployeeId; }
    public void setSourceEmployeeId(Long sourceEmployeeId) { this.sourceEmployeeId = sourceEmployeeId; }
    public String getSourceEmployeeName() { return sourceEmployeeName; }
    public void setSourceEmployeeName(String sourceEmployeeName) { this.sourceEmployeeName = sourceEmployeeName; }
    public Long getTargetEmployeeId() { return targetEmployeeId; }
    public void setTargetEmployeeId(Long targetEmployeeId) { this.targetEmployeeId = targetEmployeeId; }
    public String getTargetEmployeeName() { return targetEmployeeName; }
    public void setTargetEmployeeName(String targetEmployeeName) { this.targetEmployeeName = targetEmployeeName; }
    public Map<String, Integer> getTransferredCounts() { return transferredCounts; }
    public void setTransferredCounts(Map<String, Integer> transferredCounts) { this.transferredCounts = transferredCounts; }
    public boolean isSourceDeleted() { return sourceDeleted; }
    public void setSourceDeleted(boolean sourceDeleted) { this.sourceDeleted = sourceDeleted; }
    public int getTotalTransferred() { return totalTransferred; }
    public void setTotalTransferred(int totalTransferred) { this.totalTransferred = totalTransferred; }
}
