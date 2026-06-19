package com.workflow.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name = "monthly_lockouts")
public class MonthlyLockout implements SoftDeletable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "year_month", unique = true, nullable = false) private String yearMonth;
    @Column(name = "lock_day") private Integer lockDay;
    @Column(name = "locked_at") private LocalDateTime lockedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "locked_by") private Employee lockedBy;
    @Column(name = "is_unlocked") private Boolean isUnlocked;
    @Column(name = "unlocked_at") private LocalDateTime unlockedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "unlocked_by") private Employee unlockedBy;
    @Column(name = "unlock_reason", columnDefinition = "TEXT") private String unlockReason;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    public MonthlyLockout() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getYearMonth() { return yearMonth; } public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public Integer getLockDay() { return lockDay; } public void setLockDay(Integer lockDay) { this.lockDay = lockDay; }
    public LocalDateTime getLockedAt() { return lockedAt; } public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
    public Employee getLockedBy() { return lockedBy; } public void setLockedBy(Employee lockedBy) { this.lockedBy = lockedBy; }
    public Boolean getIsUnlocked() { return isUnlocked; } public void setIsUnlocked(Boolean isUnlocked) { this.isUnlocked = isUnlocked; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; } public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public Employee getUnlockedBy() { return unlockedBy; } public void setUnlockedBy(Employee unlockedBy) { this.unlockedBy = unlockedBy; }
    public String getUnlockReason() { return unlockReason; } public void setUnlockReason(String unlockReason) { this.unlockReason = unlockReason; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; } public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    @PrePersist protected void onCreate() {
        if (lockDay == null) lockDay = 5;
        if (isUnlocked == null) isUnlocked = false;
    }
}
