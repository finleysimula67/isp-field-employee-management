package com.workflow.dto;

public class TransferRequest {
    private Long sourceEmployeeId;
    private Long targetEmployeeId;
    private boolean deleteSource;

    public Long getSourceEmployeeId() { return sourceEmployeeId; }
    public void setSourceEmployeeId(Long sourceEmployeeId) { this.sourceEmployeeId = sourceEmployeeId; }
    public Long getTargetEmployeeId() { return targetEmployeeId; }
    public void setTargetEmployeeId(Long targetEmployeeId) { this.targetEmployeeId = targetEmployeeId; }
    public boolean isDeleteSource() { return deleteSource; }
    public void setDeleteSource(boolean deleteSource) { this.deleteSource = deleteSource; }
}
