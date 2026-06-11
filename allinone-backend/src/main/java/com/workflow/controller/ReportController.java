package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) { this.reportService = reportService; }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.generateReport(request)));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public void export(@RequestBody ReportRequest request, HttpServletResponse response) {
        reportService.exportReport(request, response);
    }
}
