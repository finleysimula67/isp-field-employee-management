package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.DailyLog;
import com.workflow.entity.Employee;
import com.workflow.service.DailyLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/daily-logs")
public class DailyLogController {
    private final DailyLogService dailyLogService;

    public DailyLogController(DailyLogService dailyLogService) { this.dailyLogService = dailyLogService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DailyLogResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        List<DailyLogResponse> logs = dailyLogService.getDailyLogs(employeeId, status, date, page, size).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DailyLogResponse>>> getMy(
            @AuthenticationPrincipal Employee employee,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyLogResponse> logs = dailyLogService.getMyLogs(employee.getId(), category, date).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/my/earnings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyEarnings(
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(dailyLogService.getEarningsSummary(employee.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DailyLogResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(dailyLogService.getDailyLog(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DailyLogResponse>> create(@RequestBody DailyLogRequest request,
                                                                 @AuthenticationPrincipal Employee employee) {
        request.setEmployeeId(employee.getId());
        return ResponseEntity.ok(ApiResponse.ok("Daily log submitted", toResponse(dailyLogService.createDailyLog(request, employee.getId()))));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DailyLogResponse>> review(@PathVariable Long id,
                                                                  @RequestBody DailyLogReviewRequest request,
                                                                  @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(dailyLogService.reviewDailyLog(id, request, employee.getId()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal Employee employee) {
        dailyLogService.softDeleteDailyLog(id, employee);
        return ResponseEntity.ok(ApiResponse.ok("Daily log deleted", null));
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody BatchDeleteRequest request,
                                                          @AuthenticationPrincipal Employee employee) {
        dailyLogService.batchDeleteDailyLogs(request.getIds(), employee);
        return ResponseEntity.ok(ApiResponse.ok("Batch delete completed", null));
    }

    @PostMapping("/batch-review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DailyLogResponse>>> batchReview(
            @RequestBody BatchReviewRequest request,
            @AuthenticationPrincipal Employee employee) {
        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus(request.getStatus());
        reviewReq.setReviewComment(request.getReviewComment());
        List<DailyLogResponse> logs = dailyLogService.batchReviewDailyLogs(request.getIds(), reviewReq, employee.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    private DailyLogResponse toResponse(DailyLog log) {
        DailyLogResponse r = new DailyLogResponse();
        r.setId(log.getId());
        r.setEmployeeId(log.getEmployee().getId());
        r.setEmployeeName(log.getEmployee().getName());
        if (log.getBranch() != null) { r.setBranchId(log.getBranch().getId()); r.setBranchName(log.getBranch().getName()); }
        r.setLogDate(log.getLogDate() != null ? log.getLogDate().toString() : null);
        r.setStartTime(log.getStartTime() != null ? log.getStartTime().toString() : null);
        r.setEndTime(log.getEndTime() != null ? log.getEndTime().toString() : null);
        r.setHoursWorked(log.getHoursWorked() != null ? log.getHoursWorked().doubleValue() : null);
        r.setCategory(log.getCategory() != null ? log.getCategory().name() : null);
        r.setLocationDescription(log.getLocationDescription());
        r.setLocationLat(log.getLocationLat() != null ? log.getLocationLat().doubleValue() : null);
        r.setLocationLng(log.getLocationLng() != null ? log.getLocationLng().doubleValue() : null);
        r.setWorkDescription(log.getWorkDescription());
        if (log.getPhotoUrls() != null && !log.getPhotoUrls().isEmpty()) {
            r.setPhotoUrls(Arrays.asList(log.getPhotoUrls().split(",")));
        }
        r.setAssignedTaskId(log.getAssignedTaskId());
        r.setStatus(log.getStatus() != null ? log.getStatus().name() : null);
        r.setReviewedBy(log.getReviewedBy() != null ? log.getReviewedBy().getId() : null);
        r.setReviewComment(log.getReviewComment());
        r.setSubmittedAt(log.getSubmittedAt() != null ? log.getSubmittedAt().toString() : null);
        r.setReviewedAt(log.getReviewedAt() != null ? log.getReviewedAt().toString() : null);
        r.setMonthLocked(log.getMonthLocked() != null ? log.getMonthLocked() : false);
        r.setIsHolidayOvertime(log.getIsHolidayOvertime() != null ? log.getIsHolidayOvertime() : false);
        return r;
    }
}
