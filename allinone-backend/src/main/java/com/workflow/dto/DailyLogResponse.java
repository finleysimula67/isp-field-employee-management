package com.workflow.dto;

public class DailyLogResponse {
    private Long id; private Long employeeId; private String employeeName;
    private Long branchId; private String branchName; private String logDate;
    private String startTime; private String endTime; private Double hoursWorked;
    private String category; private String locationDescription;
    private Double locationLat; private Double locationLng;
    private String workDescription; private String photoUrls;
    private Long assignedTaskId; private String status;
    private Long reviewedBy; private String reviewComment;
    private String submittedAt; private String reviewedAt;
    private boolean monthLocked; private boolean isHolidayOvertime;

    public DailyLogResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; } public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getLogDate() { return logDate; } public void setLogDate(String logDate) { this.logDate = logDate; }
    public String getStartTime() { return startTime; } public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; } public void setEndTime(String endTime) { this.endTime = endTime; }
    public Double getHoursWorked() { return hoursWorked; } public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
    public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }
    public Double getLocationLat() { return locationLat; } public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; } public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getWorkDescription() { return workDescription; } public void setWorkDescription(String workDescription) { this.workDescription = workDescription; }
    public String getPhotoUrls() { return photoUrls; } public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public Long getAssignedTaskId() { return assignedTaskId; } public void setAssignedTaskId(Long assignedTaskId) { this.assignedTaskId = assignedTaskId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getReviewedBy() { return reviewedBy; } public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public String getSubmittedAt() { return submittedAt; } public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; } public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public boolean isMonthLocked() { return monthLocked; } public void setMonthLocked(boolean monthLocked) { this.monthLocked = monthLocked; }
    public boolean getIsHolidayOvertime() { return isHolidayOvertime; }
    public void setIsHolidayOvertime(boolean isHolidayOvertime) { this.isHolidayOvertime = isHolidayOvertime; }
}
