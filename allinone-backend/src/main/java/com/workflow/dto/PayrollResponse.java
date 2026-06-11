package com.workflow.dto;

public class PayrollResponse {
    private Long id; private Long employeeId; private String employeeName;
    private String periodLabel; private String periodStart; private String periodEnd;
    private int daysWorked; private Double totalHours; private Double wageRateAtTime;
    private Double overtimeHours; private Double overtimeRateMultiplier;
    private Double grossPay; private Double deductions; private Double netPay;
    private String status; private String paidAt; private Long paidBy; private String paidByName;

    public PayrollResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getPeriodLabel() { return periodLabel; } public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    public String getPeriodStart() { return periodStart; } public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }
    public String getPeriodEnd() { return periodEnd; } public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }
    public int getDaysWorked() { return daysWorked; } public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }
    public Double getTotalHours() { return totalHours; } public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }
    public Double getWageRateAtTime() { return wageRateAtTime; } public void setWageRateAtTime(Double wageRateAtTime) { this.wageRateAtTime = wageRateAtTime; }
    public Double getOvertimeHours() { return overtimeHours; } public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }
    public Double getOvertimeRateMultiplier() { return overtimeRateMultiplier; }
    public void setOvertimeRateMultiplier(Double overtimeRateMultiplier) { this.overtimeRateMultiplier = overtimeRateMultiplier; }
    public Double getGrossPay() { return grossPay; } public void setGrossPay(Double grossPay) { this.grossPay = grossPay; }
    public Double getDeductions() { return deductions; } public void setDeductions(Double deductions) { this.deductions = deductions; }
    public Double getNetPay() { return netPay; } public void setNetPay(Double netPay) { this.netPay = netPay; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getPaidAt() { return paidAt; } public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
    public Long getPaidBy() { return paidBy; } public void setPaidBy(Long paidBy) { this.paidBy = paidBy; }
    public String getPaidByName() { return paidByName; } public void setPaidByName(String paidByName) { this.paidByName = paidByName; }
}
