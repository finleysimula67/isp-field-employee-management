package com.workflow.dto;

public class DayCollectionEntry {
    private Long id;
    private Double amount;
    private String status;
    private String customerName;

    public DayCollectionEntry() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Double getAmount() { return amount; } public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
}
