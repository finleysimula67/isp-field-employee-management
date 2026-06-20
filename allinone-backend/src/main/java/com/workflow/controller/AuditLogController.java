package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.entity.AuditLog;
import com.workflow.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_MANAGER')")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) { this.auditLogService = auditLogService; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAll(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        List<AuditLog> logs = auditLogService.getAuditLogs(entityType, from, to);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
