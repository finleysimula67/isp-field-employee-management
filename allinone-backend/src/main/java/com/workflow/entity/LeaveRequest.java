package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "leave_requests")
public class LeaveRequest implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false) private Employee employee;
    @Enumerated(EnumType.STRING) @Column(name = "leave_type", nullable = false) private LeaveType leaveType;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(name = "duration_days", nullable = false) private Integer durationDays;
    @Column(nullable = false, columnDefinition = "TEXT") private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LeaveStatus status;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private Employee reviewedBy;
    @Column(name = "review_comment", columnDefinition = "TEXT") private String reviewComment;
    @Column(name = "deducted_from_balance") private Boolean deductedFromBalance;
    @Column(name = "submitted_at", updatable = false) private LocalDateTime submittedAt;
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    public LeaveRequest() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public LeaveType getLeaveType() { return leaveType; } public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getDurationDays() { return durationDays; } public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public LeaveStatus getStatus() { return status; } public void setStatus(LeaveStatus status) { this.status = status; }
    public Employee getReviewedBy() { return reviewedBy; } public void setReviewedBy(Employee reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public Boolean getDeductedFromBalance() { return deductedFromBalance; }
    public void setDeductedFromBalance(Boolean deductedFromBalance) { this.deductedFromBalance = deductedFromBalance; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; } public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; } public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (status == null) status = LeaveStatus.PENDING;
        if (deductedFromBalance == null) deductedFromBalance = false;
    }
}
