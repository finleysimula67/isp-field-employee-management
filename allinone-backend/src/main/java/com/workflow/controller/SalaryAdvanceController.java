package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.SalaryAdvance;
import com.workflow.service.SalaryAdvanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salary-advances")
public class SalaryAdvanceController {
    private final SalaryAdvanceService salaryAdvanceService;

    public SalaryAdvanceController(SalaryAdvanceService salaryAdvanceService) { this.salaryAdvanceService = salaryAdvanceService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<SalaryAdvanceResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        List<SalaryAdvanceResponse> list = salaryAdvanceService.getAdvances(employeeId, status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<SalaryAdvanceResponse>>> getMy(
            @AuthenticationPrincipal Employee employee,
            @RequestParam(required = false) String status) {
        List<SalaryAdvanceResponse> list = salaryAdvanceService.getAdvances(employee.getId(), status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, java.math.BigDecimal>>> getBalance(
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(salaryAdvanceService.getEmployeeBalance(employee.getId())));
    }

    @GetMapping("/balance/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, java.math.BigDecimal>>> getBalanceForEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(salaryAdvanceService.getEmployeeBalance(employeeId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(salaryAdvanceService.getAdvance(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> request(@RequestBody SalaryAdvanceRequest request,
                                                                       @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Advance requested",
                toResponse(salaryAdvanceService.requestAdvance(request, employee.getId()))));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> review(@PathVariable Long id,
                                                                       @RequestBody AdvanceReviewRequest request,
                                                                       @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(salaryAdvanceService.reviewAdvance(id, request, employee.getId()))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal Employee employee) {
        salaryAdvanceService.softDeleteSalaryAdvance(id, employee);
        return ResponseEntity.ok(ApiResponse.ok("Salary advance deleted", null));
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody BatchDeleteRequest request,
                                                          @AuthenticationPrincipal Employee employee) {
        salaryAdvanceService.batchDeleteSalaryAdvances(request.getIds(), employee);
        return ResponseEntity.ok(ApiResponse.ok("Batch delete completed", null));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<SalaryAdvanceResponse>> manualCreate(
            @RequestBody ManualAdvanceRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Advance recorded",
                toResponse(salaryAdvanceService.createManualAdvance(request, employee.getId()))));
    }

    private SalaryAdvanceResponse toResponse(SalaryAdvance sa) {
        SalaryAdvanceResponse r = new SalaryAdvanceResponse();
        r.setId(sa.getId());
        r.setEmployeeId(sa.getEmployee().getId());
        r.setEmployeeName(sa.getEmployee().getName());
        r.setAmount(sa.getAmount() != null ? sa.getAmount().doubleValue() : 0);
        r.setRequestDate(sa.getRequestDate() != null ? sa.getRequestDate().toString() : null);
        r.setReason(sa.getReason());
        r.setStatus(sa.getStatus() != null ? sa.getStatus().name() : null);
        r.setApprovedBy(sa.getApprovedBy() != null ? sa.getApprovedBy().getId() : null);
        r.setApprovedByName(sa.getApprovedBy() != null ? sa.getApprovedBy().getName() : null);
        r.setApprovedAt(sa.getApprovedAt() != null ? sa.getApprovedAt().toString() : null);
        r.setDisbursedAt(sa.getDisbursedAt() != null ? sa.getDisbursedAt().toString() : null);
        r.setIsSettled(sa.getIsSettled() != null ? sa.getIsSettled() : false);
        r.setSettledInPayrollId(sa.getSettledInPayrollId());
        r.setNotes(sa.getNotes());
        return r;
    }
}
