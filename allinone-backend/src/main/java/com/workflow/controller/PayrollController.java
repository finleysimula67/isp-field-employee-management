package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.PayrollRecord;
import com.workflow.service.PayrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) { this.payrollService = payrollService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String periodLabel) {
        List<PayrollResponse> list = payrollService.getPayrollRecords(employeeId, periodLabel).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getMy(
            @AuthenticationPrincipal Employee employee) {
        List<PayrollResponse> list = payrollService.getPayrollRecords(employee.getId(), null).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<PayrollResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(payrollService.getPayrollRecord(id))));
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<PayrollResponse>> calculate(@RequestBody PayrollCalculateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll calculated", toResponse(payrollService.calculatePayroll(request))));
    }

    @PostMapping("/batch-calculate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> batchCalculate(@RequestBody PayrollBatchRequest request) {
        List<PayrollResponse> list = payrollService.batchCalculate(request).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Batch payroll calculated", list));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollResponse>> approve(@PathVariable Long id,
                                                                 @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll approved", toResponse(payrollService.approvePayroll(id, employee.getId()))));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollResponse>> pay(@PathVariable Long id,
                                                             @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll marked as paid", toResponse(payrollService.markAsPaid(id, employee.getId()))));
    }

    private PayrollResponse toResponse(PayrollRecord pr) {
        PayrollResponse r = new PayrollResponse();
        r.setId(pr.getId());
        r.setEmployeeId(pr.getEmployee().getId());
        r.setEmployeeName(pr.getEmployee().getName());
        r.setPeriodLabel(pr.getPeriodLabel());
        r.setPeriodStart(pr.getPeriodStart() != null ? pr.getPeriodStart().toString() : null);
        r.setPeriodEnd(pr.getPeriodEnd() != null ? pr.getPeriodEnd().toString() : null);
        r.setDaysWorked(pr.getDaysWorked() != null ? pr.getDaysWorked() : 0);
        r.setTotalHours(pr.getTotalHours() != null ? pr.getTotalHours().doubleValue() : 0);
        r.setWageRateAtTime(pr.getWageRateAtTime() != null ? pr.getWageRateAtTime().doubleValue() : 0);
        r.setOvertimeHours(pr.getOvertimeHours() != null ? pr.getOvertimeHours().doubleValue() : 0);
        r.setOvertimeRateMultiplier(pr.getOvertimeRateMultiplier() != null ? pr.getOvertimeRateMultiplier().doubleValue() : 1.50);
        r.setGrossPay(pr.getGrossPay() != null ? pr.getGrossPay().doubleValue() : 0);
        r.setDeductions(pr.getDeductions() != null ? pr.getDeductions().doubleValue() : 0);
        r.setNetPay(pr.getNetPay() != null ? pr.getNetPay().doubleValue() : 0);
        r.setStatus(pr.getStatus() != null ? pr.getStatus().name() : null);
        r.setPaidAt(pr.getPaidAt() != null ? pr.getPaidAt().toString() : null);
        r.setPaidBy(pr.getPaidBy() != null ? pr.getPaidBy().getId() : null);
        r.setPaidByName(pr.getPaidBy() != null ? pr.getPaidBy().getName() : null);
        return r;
    }
}
