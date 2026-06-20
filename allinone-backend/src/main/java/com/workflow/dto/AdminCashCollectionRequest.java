package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class AdminCashCollectionRequest {
    @NotNull private Long employeeId;
    @NotBlank private String customerName;
    private String customerPhone;
    private String customerAddress;
    @NotNull @Positive private BigDecimal amount;
    @NotBlank private String paymentMethod;
    @NotBlank private String serviceType;
    private String description;
    private Double locationLat;
    private Double locationLng;
    private String photoUrls;

    public AdminCashCollectionRequest() {}

    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getServiceType() { return serviceType; } public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public Double getLocationLat() { return locationLat; } public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; } public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getPhotoUrls() { return photoUrls; } public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
}
