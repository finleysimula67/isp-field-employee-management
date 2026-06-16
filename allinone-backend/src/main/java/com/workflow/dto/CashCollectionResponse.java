package com.workflow.dto;

public class CashCollectionResponse {
    private Long id; private Long employeeId; private String employeeName;
    private String customerName; private String customerPhone; private String customerAddress;
    private Double amount; private String paymentMethod; private String serviceType;
    private String description;
    private Double locationLat; private Double locationLng;
    private String photoUrls; private String status;
    private Long reviewedBy; private String reviewComment;
    private String submittedAt; private String reviewedAt;

    public CashCollectionResponse() {}

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public Double getAmount() { return amount; } public void setAmount(Double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getServiceType() { return serviceType; } public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public Double getLocationLat() { return locationLat; } public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; } public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getPhotoUrls() { return photoUrls; } public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getReviewedBy() { return reviewedBy; } public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewComment() { return reviewComment; } public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public String getSubmittedAt() { return submittedAt; } public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; } public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
}
