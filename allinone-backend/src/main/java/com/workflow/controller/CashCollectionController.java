package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.CashCollection;
import com.workflow.entity.Employee;
import com.workflow.service.CashCollectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cash-collections")
public class CashCollectionController {
    private final CashCollectionService cashCollectionService;

    public CashCollectionController(CashCollectionService cashCollectionService) { this.cashCollectionService = cashCollectionService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CashCollectionResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        List<CashCollectionResponse> list = cashCollectionService.getCashCollections(employeeId, status, page, size).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CashCollectionResponse>>> getMy(
            @AuthenticationPrincipal Employee employee,
            @RequestParam(required = false) String status) {
        List<CashCollectionResponse> list = cashCollectionService.getMyCashCollections(employee.getId(), status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CashCollectionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(cashCollectionService.getCashCollection(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CashCollectionResponse>> create(@RequestBody CashCollectionRequest request,
                                                                        @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Cash collection submitted",
                toResponse(cashCollectionService.createCashCollection(request, employee.getId()))));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CashCollectionResponse>> review(@PathVariable Long id,
                                                                        @RequestBody CashCollectionReviewRequest request,
                                                                        @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(cashCollectionService.reviewCashCollection(id, request, employee.getId()))));
    }

    @PostMapping("/batch-review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CashCollectionResponse>>> batchReview(
            @RequestBody BatchReviewRequest request,
            @AuthenticationPrincipal Employee employee) {
        CashCollectionReviewRequest reviewReq = new CashCollectionReviewRequest();
        reviewReq.setStatus(request.getStatus());
        reviewReq.setReviewComment(request.getReviewComment());
        List<CashCollectionResponse> list = cashCollectionService.batchReviewCashCollections(request.getIds(), reviewReq, employee.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/admin-create")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CashCollectionResponse>> adminCreate(
            @Valid @RequestBody AdminCashCollectionRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Cash collection recorded for employee",
                toResponse(cashCollectionService.createCashCollectionByAdmin(request, employee.getId()))));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CashCollectionSummaryResponse>>> getSummary(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(cashCollectionService.getMonthlySummary(month, year)));
    }

    @GetMapping("/my/summary")
    public ResponseEntity<ApiResponse<CashCollectionSummaryResponse>> getMySummary(
            @AuthenticationPrincipal Employee employee,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(cashCollectionService.getMyMonthlySummary(employee.getId(), month, year)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal Employee employee) {
        cashCollectionService.softDeleteCashCollection(id, employee);
        return ResponseEntity.ok(ApiResponse.ok("Cash collection deleted", null));
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody BatchDeleteRequest request,
                                                          @AuthenticationPrincipal Employee employee) {
        cashCollectionService.batchDeleteCashCollections(request.getIds(), employee);
        return ResponseEntity.ok(ApiResponse.ok("Batch delete completed", null));
    }

    private CashCollectionResponse toResponse(CashCollection c) {
        CashCollectionResponse r = new CashCollectionResponse();
        r.setId(c.getId());
        r.setEmployeeId(c.getEmployee().getId());
        r.setEmployeeName(c.getEmployee().getName());
        r.setCustomerName(c.getCustomerName());
        r.setCustomerPhone(c.getCustomerPhone());
        r.setCustomerAddress(c.getCustomerAddress());
        r.setAmount(c.getAmount());
        r.setPaymentMethod(c.getPaymentMethod() != null ? c.getPaymentMethod().name() : null);
        r.setServiceType(c.getServiceType() != null ? c.getServiceType().name() : null);
        r.setDescription(c.getDescription());
        r.setLocationLat(c.getLocationLat() != null ? c.getLocationLat().doubleValue() : null);
        r.setLocationLng(c.getLocationLng() != null ? c.getLocationLng().doubleValue() : null);
        r.setPhotoUrls(c.getPhotoUrls());
        r.setStatus(c.getStatus() != null ? c.getStatus().name() : null);
        r.setReviewedBy(c.getReviewedBy() != null ? c.getReviewedBy().getId() : null);
        r.setReviewComment(c.getReviewComment());
        r.setSubmittedAt(c.getSubmittedAt() != null ? c.getSubmittedAt().toString() : null);
        r.setReviewedAt(c.getReviewedAt() != null ? c.getReviewedAt().toString() : null);
        return r;
    }
}
