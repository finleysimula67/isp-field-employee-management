package com.workflow.dto;

import java.math.BigDecimal;

public class DayCollectionEntry {
    private Long id;
    private BigDecimal amount;
    private String status;
    private String customerName;

    public DayCollectionEntry() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
}
