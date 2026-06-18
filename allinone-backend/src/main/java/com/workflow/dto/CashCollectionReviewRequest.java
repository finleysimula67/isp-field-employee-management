package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class CashCollectionReviewRequest {
    @NotBlank private String status;
    private String reviewComment;
    private Double amount;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String description;
    private String paymentMethod;
    private String serviceType;

    public CashCollectionReviewRequest() {}

    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public Double getAmount() { return amount; } public void setAmount(Double amount) { this.amount = amount; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getServiceType() { return serviceType; } public void setServiceType(String serviceType) { this.serviceType = serviceType; }
}
