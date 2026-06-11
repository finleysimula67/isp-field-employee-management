package com.workflow.dto;

public class LockoutStatusResponse {
    private Long id; private String yearMonth; private boolean isLocked;
    private String lockedAt; private Long lockedBy; private String lockedByName;
    private int lockDay; private boolean isUnlocked; private String unlockedAt;
    private String unlockedReason;

    public LockoutStatusResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getYearMonth() { return yearMonth; } public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public boolean getIsLocked() { return isLocked; } public void setIsLocked(boolean isLocked) { this.isLocked = isLocked; }
    public String getLockedAt() { return lockedAt; } public void setLockedAt(String lockedAt) { this.lockedAt = lockedAt; }
    public Long getLockedBy() { return lockedBy; } public void setLockedBy(Long lockedBy) { this.lockedBy = lockedBy; }
    public String getLockedByName() { return lockedByName; } public void setLockedByName(String lockedByName) { this.lockedByName = lockedByName; }
    public int getLockDay() { return lockDay; } public void setLockDay(int lockDay) { this.lockDay = lockDay; }
    public boolean getIsUnlocked() { return isUnlocked; } public void setIsUnlocked(boolean isUnlocked) { this.isUnlocked = isUnlocked; }
    public String getUnlockedAt() { return unlockedAt; } public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
    public String getUnlockedReason() { return unlockedReason; } public void setUnlockedReason(String unlockedReason) { this.unlockedReason = unlockedReason; }
}
