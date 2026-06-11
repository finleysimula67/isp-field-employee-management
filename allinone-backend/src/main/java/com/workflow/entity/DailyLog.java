package com.workflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "daily_logs")
public class DailyLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "branch_id") private Branch branch;
    @Column(name = "log_date", nullable = false) private LocalDate logDate;
    @Column(name = "start_time") private LocalTime startTime;
    @Column(name = "end_time") private LocalTime endTime;
    @Column(name = "hours_worked") private BigDecimal hoursWorked;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LogCategory category;
    @Column(name = "location_description", columnDefinition = "TEXT") private String locationDescription;
    @Column(name = "location_lat") private BigDecimal locationLat;
    @Column(name = "location_lng") private BigDecimal locationLng;
    @Column(name = "work_description", nullable = false, columnDefinition = "TEXT") private String workDescription;
    @Column(name = "photo_urls", columnDefinition = "TEXT") private String photoUrls;
    @Column(name = "assigned_task_id") private Long assignedTaskId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LogStatus status;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private Employee reviewedBy;
    @Column(name = "review_comment", columnDefinition = "TEXT") private String reviewComment;
    @Column(name = "submitted_at", updatable = false) private LocalDateTime submittedAt;
    @Column(name = "synced_at") private LocalDateTime syncedAt;
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
    @Column(name = "month_locked") private Boolean monthLocked;
    @Column(name = "is_holiday_overtime") private Boolean isHolidayOvertime;

    public DailyLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public BigDecimal getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }
    public LogCategory getCategory() { return category; }
    public void setCategory(LogCategory category) { this.category = category; }
    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }
    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }
    public BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(BigDecimal locationLng) { this.locationLng = locationLng; }
    public String getWorkDescription() { return workDescription; }
    public void setWorkDescription(String workDescription) { this.workDescription = workDescription; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public Long getAssignedTaskId() { return assignedTaskId; }
    public void setAssignedTaskId(Long assignedTaskId) { this.assignedTaskId = assignedTaskId; }
    public LogStatus getStatus() { return status; }
    public void setStatus(LogStatus status) { this.status = status; }
    public Employee getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Employee reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public Boolean getMonthLocked() { return monthLocked; }
    public void setMonthLocked(Boolean monthLocked) { this.monthLocked = monthLocked; }
    public Boolean getIsHolidayOvertime() { return isHolidayOvertime; }
    public void setIsHolidayOvertime(Boolean isHolidayOvertime) { this.isHolidayOvertime = isHolidayOvertime; }

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (status == null) status = LogStatus.PENDING;
        if (monthLocked == null) monthLocked = false;
        if (isHolidayOvertime == null) isHolidayOvertime = false;
    }
}
