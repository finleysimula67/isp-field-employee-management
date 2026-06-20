package com.workflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_logs")
public class TransferLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sourceEmployeeId;
    private String sourceEmployeeName;
    private Long targetEmployeeId;
    private String targetEmployeeName;

    @Column(length = 5000)
    private String summaryJson;

    private boolean sourceDeleted;

    private LocalDateTime transferredAt;
    private Long transferredBy;

    public TransferLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceEmployeeId() { return sourceEmployeeId; }
    public void setSourceEmployeeId(Long sourceEmployeeId) { this.sourceEmployeeId = sourceEmployeeId; }
    public String getSourceEmployeeName() { return sourceEmployeeName; }
    public void setSourceEmployeeName(String sourceEmployeeName) { this.sourceEmployeeName = sourceEmployeeName; }
    public Long getTargetEmployeeId() { return targetEmployeeId; }
    public void setTargetEmployeeId(Long targetEmployeeId) { this.targetEmployeeId = targetEmployeeId; }
    public String getTargetEmployeeName() { return targetEmployeeName; }
    public void setTargetEmployeeName(String targetEmployeeName) { this.targetEmployeeName = targetEmployeeName; }
    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }
    public boolean isSourceDeleted() { return sourceDeleted; }
    public void setSourceDeleted(boolean sourceDeleted) { this.sourceDeleted = sourceDeleted; }
    public LocalDateTime getTransferredAt() { return transferredAt; }
    public void setTransferredAt(LocalDateTime transferredAt) { this.transferredAt = transferredAt; }
    public Long getTransferredBy() { return transferredBy; }
    public void setTransferredBy(Long transferredBy) { this.transferredBy = transferredBy; }
}
