package com.workflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SalaryAdvanceRequest {
    @NotNull @Positive private Double amount;
    private String reason;

    public SalaryAdvanceRequest() {}

    public Double getAmount() { return amount; } public void setAmount(Double amount) { this.amount = amount; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
}
