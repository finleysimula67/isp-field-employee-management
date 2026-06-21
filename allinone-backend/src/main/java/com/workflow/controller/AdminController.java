package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.dto.PurgeResult;
import com.workflow.entity.Employee;
import com.workflow.repository.EmployeeRepository;
import com.workflow.service.AuditLogService;
import com.workflow.service.PurgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PurgeService purgeService;
    private final AuditLogService auditLogService;
    private final EmployeeRepository employeeRepository;

    public AdminController(PurgeService purgeService, AuditLogService auditLogService,
                           EmployeeRepository employeeRepository) {
        this.purgeService = purgeService;
        this.auditLogService = auditLogService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/purge")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PurgeResult>> purge(@AuthenticationPrincipal Employee admin) {
        PurgeResult result = purgeService.purgeAllData();
        auditLogService.logWithActor("System", null, "PURGE_ALL_DATA", admin,
                null, null, "Purged all transactional data. Total deleted: " + result.getTotalDeleted());
        return ResponseEntity.ok(ApiResponse.ok("Factory reset complete", result));
    }

    @PostMapping("/logout-user/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> forceLogout(@PathVariable Long id,
                                                          @AuthenticationPrincipal Employee admin) {
        employeeRepository.incrementTokenVersion(id);
        auditLogService.logWithActor("Employee", id, "FORCE_LOGOUT", admin,
                null, null, "Admin forced logout for user " + id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User logged out successfully", null));
    }
}
