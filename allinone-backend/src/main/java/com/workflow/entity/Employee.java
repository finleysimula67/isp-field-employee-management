package com.workflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "branch_id")
    private Branch branch;
    @Column(name = "auth_provider_id") private String authProviderId;
    @Enumerated(EnumType.STRING) @Column(name = "auth_type") private AuthType authType;
    @Column(name = "password_hash") private String passwordHash;
    @Column(name = "is_active") private Boolean isActive;
    @Column(name = "is_account_approved") private Boolean isAccountApproved;
    @Enumerated(EnumType.STRING) @Column(name = "wage_type") private WageType wageType;
    @Column(name = "daily_rate") private BigDecimal dailyRate;
    @Column(name = "hourly_wage") private BigDecimal hourlyWage;
    @Column(name = "total_leave_days_per_year") private BigDecimal totalLeaveDaysPerYear;
    @Column(name = "remaining_leave_days") private BigDecimal remainingLeaveDays;
    @Column(name = "carry_over_leave") private BigDecimal carryOverLeave;
    @Column(name = "reset_token") private String resetToken;
    @Column(name = "reset_token_expiry") private LocalDateTime resetTokenExpiry;
    @Column(name = "phone") private String phone;
    @Column(name = "is_owner") private Boolean isOwner;
    @Column(name = "max_advance_limit") private BigDecimal maxAdvanceLimit;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Version @Column(name = "version") private Integer version;

    public Employee() {}

    public Employee(String email, String name, Role role, Branch branch, String authProviderId,
                    AuthType authType, String passwordHash, Boolean isActive, Boolean isAccountApproved,
                    WageType wageType, BigDecimal dailyRate, BigDecimal hourlyWage,
                    BigDecimal remainingLeaveDays, BigDecimal carryOverLeave, BigDecimal maxAdvanceLimit) {
        this.email = email; this.name = name; this.role = role; this.branch = branch;
        this.authProviderId = authProviderId; this.authType = authType; this.passwordHash = passwordHash;
        this.isActive = isActive; this.isAccountApproved = isAccountApproved; this.wageType = wageType;
        this.dailyRate = dailyRate; this.hourlyWage = hourlyWage;
        this.remainingLeaveDays = remainingLeaveDays; this.carryOverLeave = carryOverLeave;
        this.isOwner = false; this.maxAdvanceLimit = maxAdvanceLimit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public String getAuthProviderId() { return authProviderId; }
    public void setAuthProviderId(String authProviderId) { this.authProviderId = authProviderId; }
    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsAccountApproved() { return isAccountApproved; }
    public void setIsAccountApproved(Boolean isAccountApproved) { this.isAccountApproved = isAccountApproved; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getIsOwner() { return isOwner; }
    public void setIsOwner(Boolean isOwner) { this.isOwner = isOwner; }
    public BigDecimal getMaxAdvanceLimit() { return maxAdvanceLimit; }
    public void setMaxAdvanceLimit(BigDecimal maxAdvanceLimit) { this.maxAdvanceLimit = maxAdvanceLimit; }
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
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isAccountApproved == null) isAccountApproved = false;
        if (isOwner == null) isOwner = false;
        if (remainingLeaveDays == null) remainingLeaveDays = BigDecimal.ZERO;
        if (carryOverLeave == null) carryOverLeave = BigDecimal.ZERO;
        if (maxAdvanceLimit == null) maxAdvanceLimit = BigDecimal.valueOf(5000);
    }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
