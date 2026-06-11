package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.entity.EmailAllowList;
import com.workflow.entity.Employee;
import com.workflow.repository.EmailAllowListRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-allow-list")
public class EmailAllowListController {
    private final EmailAllowListRepository repository;

    public EmailAllowListController(EmailAllowListRepository repository) { this.repository = repository; }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmailAllowList>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(repository.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmailAllowList>> add(@RequestBody Map<String, String> body,
                                                           @AuthenticationPrincipal Employee employee) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        email = email.trim().toLowerCase();
        if (repository.existsByEmail(email))
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already in allow list"));
        EmailAllowList entry = new EmailAllowList(email);
        entry.setAddedBy(employee);
        return ResponseEntity.ok(ApiResponse.ok("Email added to allow list", repository.save(entry)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Email removed from allow list", null));
    }
}
