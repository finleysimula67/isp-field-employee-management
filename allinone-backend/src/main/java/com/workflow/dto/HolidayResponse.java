package com.workflow.dto;

public class HolidayResponse {
    private Long id;
    private String date;
    private String name;
    private boolean isRecurringYearly;
    private boolean overtimeApplies;
    private Long createdBy;
    private String createdByName;

    public HolidayResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean getIsRecurringYearly() { return isRecurringYearly; }
    public void setIsRecurringYearly(boolean isRecurringYearly) { this.isRecurringYearly = isRecurringYearly; }
    public boolean getOvertimeApplies() { return overtimeApplies; }
    public void setOvertimeApplies(boolean overtimeApplies) { this.overtimeApplies = overtimeApplies; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
}
