package com.workflow.dto;

import java.util.List;
import java.util.Map;

public class AttendanceResponse {
    private Long employeeId;
    private String employeeName;
    private Map<String, String> days;
    private AttendanceStats stats;

    public AttendanceResponse() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Map<String, String> getDays() { return days; }
    public void setDays(Map<String, String> days) { this.days = days; }
    public AttendanceStats getStats() { return stats; }
    public void setStats(AttendanceStats stats) { this.stats = stats; }

    public static class AttendanceStats {
        private long present;
        private long absent;
        private long pending;
        private long onLeave;
        private long holiday;

        public AttendanceStats() {}

        public long getPresent() { return present; }
        public void setPresent(long present) { this.present = present; }
        public long getAbsent() { return absent; }
        public void setAbsent(long absent) { this.absent = absent; }
        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }
        public long getOnLeave() { return onLeave; }
        public void setOnLeave(long onLeave) { this.onLeave = onLeave; }
        public long getHoliday() { return holiday; }
        public void setHoliday(long holiday) { this.holiday = holiday; }
    }
}
