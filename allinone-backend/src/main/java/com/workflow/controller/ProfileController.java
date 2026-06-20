package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final EmployeeService employeeService;

    public ProfileController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> getProfile(@AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getEmployee(employee.getId())));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateProfile(
            @AuthenticationPrincipal Employee employee,
            @RequestBody UpdateProfileRequest request) {
        EmployeeResponse updated = employeeService.updateProfile(employee.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", updated));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Employee employee,
            @RequestBody ChangePasswordRequest request) {
        try {
            employeeService.changePassword(employee.getId(), request);
            return ResponseEntity.ok(ApiResponse.ok("Password changed", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
