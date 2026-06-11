package com.workflow.dto;

public class HolidayRequest {
    private String date;
    private String name;
    private Boolean isRecurringYearly;
    private Boolean overtimeApplies;

    public HolidayRequest() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsRecurringYearly() { return isRecurringYearly; }
    public void setIsRecurringYearly(Boolean isRecurringYearly) { this.isRecurringYearly = isRecurringYearly; }
    public Boolean getOvertimeApplies() { return overtimeApplies; }
    public void setOvertimeApplies(Boolean overtimeApplies) { this.overtimeApplies = overtimeApplies; }
}
