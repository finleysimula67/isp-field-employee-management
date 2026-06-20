package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.entity.EmailAllowList;
import com.workflow.entity.Employee;
import com.workflow.service.EmailAllowListService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-allow-list")
public class EmailAllowListController {
    private final EmailAllowListService service;

    public EmailAllowListController(EmailAllowListService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmailAllowList>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmailAllowList>> add(@RequestBody Map<String, String> body,
                                                           @AuthenticationPrincipal Employee employee) {
        try {
            EmailAllowList entry = service.addEmail(body.get("email"), employee);
            return ResponseEntity.status(201).body(ApiResponse.ok("Email added to allow list", entry));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        service.removeById(id);
        return ResponseEntity.status(204).build();
    }
}
