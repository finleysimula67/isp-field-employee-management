package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class LockoutUnlockRequest {
    @NotBlank private String reason;

    public LockoutUnlockRequest() {}

    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
}
