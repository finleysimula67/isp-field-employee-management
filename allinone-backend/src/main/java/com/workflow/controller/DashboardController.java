package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.entity.Employee;
import com.workflow.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getStats()));
    }

    @GetMapping("/employee-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeStats(
            @AuthenticationPrincipal Employee employee) {
        Map<String, Object> stats = new java.util.HashMap<>();
        if (employee == null) return ResponseEntity.ok(ApiResponse.ok(stats));
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getEmployeeStats(employee.getId())));
    }

    @GetMapping("/employee-stats/{empId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeStatsById(@PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getEmployeeStats(empId)));
    }
}
