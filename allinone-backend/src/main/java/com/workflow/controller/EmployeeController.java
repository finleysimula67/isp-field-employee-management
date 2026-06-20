package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) { this.employeeService = employeeService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getAllEmployees()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getEmployee(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(@RequestBody EmployeeRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Employee created", employeeService.createEmployee(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(@PathVariable Long id, @RequestBody EmployeeRequest r) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.updateEmployee(id, r)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Employee approved", employeeService.approveEmployee(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.status(204).build();
    }

    @PutMapping("/transfer-ownership/{targetId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> transferOwnership(
            @AuthenticationPrincipal Employee current,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(ApiResponse.ok("Ownership transferred",
                employeeService.transferOwnership(current.getId(), targetId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmployeeResponse>> me(@AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getEmployee(employee.getId())));
    }
}
