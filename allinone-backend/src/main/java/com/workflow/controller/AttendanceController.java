package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.dto.AttendanceResponse;
import com.workflow.dto.WageSummaryResponse;
import com.workflow.entity.Employee;
import com.workflow.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMonthly(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getMonthlyAttendance(month, year)));
    }

    @GetMapping("/wages")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<WageSummaryResponse>>> getWages(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getWageSummary(month, year)));
    }

    @GetMapping("/my/monthly")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getMyMonthly(
            @AuthenticationPrincipal Employee employee,
            @RequestParam int month, @RequestParam int year) {
        AttendanceResponse res = attendanceService.getMyMonthlyAttendance(employee.getId(), month, year);
        res.setEmployeeName(employee.getName());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/my/wages")
    public ResponseEntity<ApiResponse<WageSummaryResponse>> getMyWages(
            @AuthenticationPrincipal Employee employee,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getMyWageSummary(employee.getId(), month, year)));
    }
}
