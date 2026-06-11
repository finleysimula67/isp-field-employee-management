package com.workflow.dto;

import java.time.LocalDateTime;

public class BranchResponse {
    private Long id; private String name; private String code; private String address;
    private String managerName; private Long managerId;
    private Boolean isActive; private LocalDateTime createdAt;

    public BranchResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getCode() { return code; } public void setCode(String code) { this.code = code; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
    public String getManagerName() { return managerName; } public void setManagerName(String managerName) { this.managerName = managerName; }
    public Long getManagerId() { return managerId; } public void setManagerId(Long managerId) { this.managerId = managerId; }
    public Boolean getIsActive() { return isActive; } public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
