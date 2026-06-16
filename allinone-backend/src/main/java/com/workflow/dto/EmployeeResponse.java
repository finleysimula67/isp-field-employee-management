package com.workflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmployeeResponse {
    private Long id; private String email; private String name; private String phone; private String role;
    private String branchName; private Long branchId; private Boolean isActive;
    private Boolean isAccountApproved; private String wageType;
    private BigDecimal dailyRate; private BigDecimal hourlyWage;
    private BigDecimal totalLeaveDaysPerYear;
    private BigDecimal remainingLeaveDays; private BigDecimal carryOverLeave; private Boolean isOwner;
    private BigDecimal maxAdvanceLimit;
    private LocalDateTime createdAt;

    public EmployeeResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; } public void setRole(String role) { this.role = role; }
    public String getBranchName() { return branchName; } public void setBranchName(String branchName) { this.branchName = branchName; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Boolean getIsActive() { return isActive; } public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsAccountApproved() { return isAccountApproved; }
    public void setIsAccountApproved(Boolean isAccountApproved) { this.isAccountApproved = isAccountApproved; }
    public String getWageType() { return wageType; } public void setWageType(String wageType) { this.wageType = wageType; }
    public BigDecimal getDailyRate() { return dailyRate; } public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getHourlyWage() { return hourlyWage; } public void setHourlyWage(BigDecimal hourlyWage) { this.hourlyWage = hourlyWage; }
    public BigDecimal getTotalLeaveDaysPerYear() { return totalLeaveDaysPerYear; }
    public void setTotalLeaveDaysPerYear(BigDecimal totalLeaveDaysPerYear) { this.totalLeaveDaysPerYear = totalLeaveDaysPerYear; }
    public BigDecimal getRemainingLeaveDays() { return remainingLeaveDays; }
    public void setRemainingLeaveDays(BigDecimal remainingLeaveDays) { this.remainingLeaveDays = remainingLeaveDays; }
    public BigDecimal getCarryOverLeave() { return carryOverLeave; }
    public void setCarryOverLeave(BigDecimal carryOverLeave) { this.carryOverLeave = carryOverLeave; }
    public Boolean getIsOwner() { return isOwner; } public void setIsOwner(Boolean isOwner) { this.isOwner = isOwner; }
    public BigDecimal getMaxAdvanceLimit() { return maxAdvanceLimit; } public void setMaxAdvanceLimit(BigDecimal maxAdvanceLimit) { this.maxAdvanceLimit = maxAdvanceLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
