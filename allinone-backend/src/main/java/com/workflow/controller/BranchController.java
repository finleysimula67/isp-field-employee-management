package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.dto.BranchResponse;
import com.workflow.entity.Employee;
import com.workflow.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) { this.branchService = branchService; }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(branchService.getAllBranches()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BranchResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(branchService.getBranch(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> create(
            @RequestParam String name, @RequestParam(required = false) String code,
            @RequestParam(required = false) String address) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Branch created", branchService.createBranch(name, code, address)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal Employee employee) {
        branchService.softDeleteBranch(id, employee);
        return ResponseEntity.status(204).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> update(
            @PathVariable Long id, @RequestParam(required = false) String name,
            @RequestParam(required = false) String code, @RequestParam(required = false) String address,
            @RequestParam(required = false) Long managerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                branchService.updateBranch(id, name, code, address, managerId)));
    }
}
