package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "holidays")
public class Holiday implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private LocalDate date;
    @Column(nullable = false) private String name;
    @Column(name = "is_recurring_yearly") private Boolean isRecurringYearly;
    @Column(name = "overtime_applies") private Boolean overtimeApplies;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private Employee createdBy;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    public Holiday() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; } public void setDate(LocalDate date) { this.date = date; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Boolean getIsRecurringYearly() { return isRecurringYearly; }
    public void setIsRecurringYearly(Boolean isRecurringYearly) { this.isRecurringYearly = isRecurringYearly; }
    public Boolean getOvertimeApplies() { return overtimeApplies; }
    public void setOvertimeApplies(Boolean overtimeApplies) { this.overtimeApplies = overtimeApplies; }
    public Employee getCreatedBy() { return createdBy; } public void setCreatedBy(Employee createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; } public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() {
        if (isRecurringYearly == null) isRecurringYearly = false;
        if (overtimeApplies == null) overtimeApplies = false;
    }
}
