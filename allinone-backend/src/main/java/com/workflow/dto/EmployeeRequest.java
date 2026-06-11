package com.workflow.dto;

import com.workflow.entity.Role;
import com.workflow.entity.WageType;
import com.workflow.entity.AuthType;
import java.math.BigDecimal;

public class EmployeeRequest {
    private String email;
    private String name;
    private Role role;
    private Long branchId;
    private String password;
    private AuthType authType;
    private WageType wageType;
    private BigDecimal dailyRate;
    private BigDecimal hourlyWage;
    private BigDecimal totalLeaveDaysPerYear;
    private BigDecimal remainingLeaveDays;
    private BigDecimal carryOverLeave;
    private BigDecimal maxAdvanceLimit;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }
    public WageType getWageType() { return wageType; }
    public void setWageType(WageType wageType) { this.wageType = wageType; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(BigDecimal hourlyWage) { this.hourlyWage = hourlyWage; }
    public BigDecimal getTotalLeaveDaysPerYear() { return totalLeaveDaysPerYear; }
    public void setTotalLeaveDaysPerYear(BigDecimal totalLeaveDaysPerYear) { this.totalLeaveDaysPerYear = totalLeaveDaysPerYear; }
    public BigDecimal getRemainingLeaveDays() { return remainingLeaveDays; }
    public void setRemainingLeaveDays(BigDecimal remainingLeaveDays) { this.remainingLeaveDays = remainingLeaveDays; }
    public BigDecimal getCarryOverLeave() { return carryOverLeave; }
    public void setCarryOverLeave(BigDecimal carryOverLeave) { this.carryOverLeave = carryOverLeave; }
    public BigDecimal getMaxAdvanceLimit() { return maxAdvanceLimit; }
    public void setMaxAdvanceLimit(BigDecimal maxAdvanceLimit) { this.maxAdvanceLimit = maxAdvanceLimit; }
}
