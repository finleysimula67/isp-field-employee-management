package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public class DailyLogRequest {
    private Long employeeId; private Long branchId; private String logDate;
    private String startTime; private String endTime; private BigDecimal hoursWorked;
    private String category; private String locationDescription;
    private Double locationLat; private Double locationLng;
    @NotBlank private String workDescription; private List<String> photoUrls; private Long assignedTaskId;

    public DailyLogRequest() {}

    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getLogDate() { return logDate; } public void setLogDate(String logDate) { this.logDate = logDate; }
    public String getStartTime() { return startTime; } public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; } public void setEndTime(String endTime) { this.endTime = endTime; }
    public BigDecimal getHoursWorked() { return hoursWorked; } public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }
    public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }
    public Double getLocationLat() { return locationLat; } public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; } public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getWorkDescription() { return workDescription; } public void setWorkDescription(String workDescription) { this.workDescription = workDescription; }
    public List<String> getPhotoUrls() { return photoUrls; } public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    public Long getAssignedTaskId() { return assignedTaskId; } public void setAssignedTaskId(Long assignedTaskId) { this.assignedTaskId = assignedTaskId; }
}
