package com.workflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name = "salary_advances")
public class SalaryAdvance implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false) private Employee employee;
    @Column(nullable = false) private BigDecimal amount;
    @Column(name = "request_date", nullable = false) private LocalDate requestDate;
    @Column(columnDefinition = "TEXT") private String reason;
    @Enumerated(EnumType.STRING) private AdvanceStatus status;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by") private Employee approvedBy;
    @Column(name = "approved_at") private LocalDateTime approvedAt;
    @Column(name = "disbursed_at") private LocalDateTime disbursedAt;
    @Column(name = "is_settled") private Boolean isSettled;
    @Column(name = "settled_in_payroll_id") private Long settledInPayrollId;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    public SalaryAdvance() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getRequestDate() { return requestDate; } public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public AdvanceStatus getStatus() { return status; } public void setStatus(AdvanceStatus status) { this.status = status; }
    public Employee getApprovedBy() { return approvedBy; } public void setApprovedBy(Employee approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; } public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getDisbursedAt() { return disbursedAt; } public void setDisbursedAt(LocalDateTime disbursedAt) { this.disbursedAt = disbursedAt; }
    public Boolean getIsSettled() { return isSettled; } public void setIsSettled(Boolean isSettled) { this.isSettled = isSettled; }
    public Long getSettledInPayrollId() { return settledInPayrollId; }
    public void setSettledInPayrollId(Long settledInPayrollId) { this.settledInPayrollId = settledInPayrollId; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; } public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() {
        if (status == null) status = AdvanceStatus.PENDING;
        if (isSettled == null) isSettled = false;
    }
}
