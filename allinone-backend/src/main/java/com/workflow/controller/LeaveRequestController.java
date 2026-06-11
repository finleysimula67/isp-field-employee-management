package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.LeaveRequest;
import com.workflow.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) { this.leaveRequestService = leaveRequestService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        List<LeaveRequestResponse> list = leaveRequestService.getLeaveRequests(employeeId, status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getMy(
            @AuthenticationPrincipal Employee employee,
            @RequestParam(required = false) String status) {
        List<LeaveRequestResponse> list = leaveRequestService.getLeaveRequests(employee.getId(), status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(leaveRequestService.getLeaveRequest(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> create(@RequestBody LeaveRequestRequest request,
                                                                     @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Leave request submitted",
                toResponse(leaveRequestService.createLeaveRequest(request, employee.getId()))));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> review(@PathVariable Long id,
                                                                      @RequestBody LeaveReviewRequest request,
                                                                      @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(leaveRequestService.reviewLeaveRequest(id, request, employee.getId()))));
    }

    @PostMapping("/batch-review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> batchReview(
            @RequestBody BatchReviewRequest request,
            @AuthenticationPrincipal Employee employee) {
        LeaveReviewRequest reviewReq = new LeaveReviewRequest();
        reviewReq.setStatus(request.getStatus());
        reviewReq.setReviewComment(request.getReviewComment());
        List<LeaveRequestResponse> list = leaveRequestService.batchReviewLeaveRequests(request.getIds(), reviewReq, employee.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    private LeaveRequestResponse toResponse(LeaveRequest lr) {
        LeaveRequestResponse r = new LeaveRequestResponse();
        r.setId(lr.getId());
        r.setEmployeeId(lr.getEmployee().getId());
        r.setEmployeeName(lr.getEmployee().getName());
        r.setLeaveType(lr.getLeaveType() != null ? lr.getLeaveType().name() : null);
        r.setStartDate(lr.getStartDate() != null ? lr.getStartDate().toString() : null);
        r.setEndDate(lr.getEndDate() != null ? lr.getEndDate().toString() : null);
        r.setDurationDays(lr.getDurationDays() != null ? lr.getDurationDays() : 0);
        r.setReason(lr.getReason());
        r.setStatus(lr.getStatus() != null ? lr.getStatus().name() : null);
        r.setReviewedBy(lr.getReviewedBy() != null ? lr.getReviewedBy().getId() : null);
        r.setReviewedByName(lr.getReviewedBy() != null ? lr.getReviewedBy().getName() : null);
        r.setReviewComment(lr.getReviewComment());
        r.setDeductedFromBalance(lr.getDeductedFromBalance() != null ? lr.getDeductedFromBalance() : false);
        r.setSubmittedAt(lr.getSubmittedAt() != null ? lr.getSubmittedAt().toString() : null);
        r.setReviewedAt(lr.getReviewedAt() != null ? lr.getReviewedAt().toString() : null);
        return r;
    }
}
