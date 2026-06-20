package com.workflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class SalaryAdvanceRequest {
    @NotNull @Positive private BigDecimal amount;
    private String reason;

    public SalaryAdvanceRequest() {}

    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
}
