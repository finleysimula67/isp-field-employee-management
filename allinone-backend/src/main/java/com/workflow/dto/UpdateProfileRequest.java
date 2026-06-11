package com.workflow.dto;

import java.math.BigDecimal;

public class UpdateProfileRequest {
    private String name;
    private BigDecimal dailyRate;
    private BigDecimal hourlyWage;
    private String wageType;

    public UpdateProfileRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(BigDecimal hourlyWage) { this.hourlyWage = hourlyWage; }
    public String getWageType() { return wageType; }
    public void setWageType(String wageType) { this.wageType = wageType; }
}
