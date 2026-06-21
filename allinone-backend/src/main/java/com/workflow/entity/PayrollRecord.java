package com.workflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name = "payroll_records")
public class PayrollRecord implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false) private Employee employee;
    @Column(name = "period_label", nullable = false) private String periodLabel;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "days_worked") private Integer daysWorked;
    @Column(name = "total_hours") private BigDecimal totalHours;
    @Column(name = "wage_rate_at_time") private BigDecimal wageRateAtTime;
    @Column(name = "overtime_hours") private BigDecimal overtimeHours;
    @Column(name = "overtime_rate_multiplier") private BigDecimal overtimeRateMultiplier;
    @Column(name = "gross_pay") private BigDecimal grossPay;
    private BigDecimal deductions;
    @Column(name = "net_pay") private BigDecimal netPay;
    @Enumerated(EnumType.STRING) private PayrollStatus status;
    @Column(name = "paid_at") private LocalDateTime paidAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "paid_by") private Employee paidBy;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;
    public PayrollRecord() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; } public void setEmployee(Employee employee) { this.employee = employee; }
    public String getPeriodLabel() { return periodLabel; } public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    public LocalDate getPeriodStart() { return periodStart; } public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; } public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public Integer getDaysWorked() { return daysWorked; } public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }
    public BigDecimal getTotalHours() { return totalHours; } public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public BigDecimal getWageRateAtTime() { return wageRateAtTime; } public void setWageRateAtTime(BigDecimal wageRateAtTime) { this.wageRateAtTime = wageRateAtTime; }
    public BigDecimal getOvertimeHours() { return overtimeHours; } public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public BigDecimal getOvertimeRateMultiplier() { return overtimeRateMultiplier; }
    public BigDecimal getGrossPay() { return grossPay; } public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }
    public BigDecimal getDeductions() { return deductions; } public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getNetPay() { return netPay; } public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public PayrollStatus getStatus() { return status; } public void setStatus(PayrollStatus status) { this.status = status; }
    public LocalDateTime getPaidAt() { return paidAt; } public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public Employee getPaidBy() { return paidBy; } public void setPaidBy(Employee paidBy) { this.paidBy = paidBy; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; } public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() {
        if (status == null) status = PayrollStatus.DRAFT;
        if (overtimeHours == null) overtimeHours = BigDecimal.ZERO;
        if (overtimeRateMultiplier == null) overtimeRateMultiplier = new BigDecimal("1.50");
        if (deductions == null) deductions = BigDecimal.ZERO;
    }
}
