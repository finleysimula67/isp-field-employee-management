package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.AuthType;
import com.workflow.entity.Employee;
import com.workflow.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> getProfile(@AuthenticationPrincipal Employee employee) {
        Employee fresh = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(fresh)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateProfile(
            @AuthenticationPrincipal Employee employee,
            @RequestBody UpdateProfileRequest request) {
        Employee e = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (request.getName() != null) e.setName(request.getName());
        if (request.getPhone() != null) e.setPhone(request.getPhone());
        Employee saved = employeeRepository.save(e);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", toResponse(saved)));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Employee employee,
            @RequestBody ChangePasswordRequest request) {
        Employee e = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (e.getAuthType() == AuthType.GOOGLE_ONLY)
            return ResponseEntity.badRequest().body(ApiResponse.error("Google accounts don't have a password"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), e.getPasswordHash()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Current password is incorrect"));
        e.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(e);
        return ResponseEntity.ok(ApiResponse.ok("Password changed", null));
    }

    private EmployeeResponse toResponse(Employee e) {
        EmployeeResponse r = new EmployeeResponse();
        r.setId(e.getId());
        r.setEmail(e.getEmail());
        r.setName(e.getName());
        r.setPhone(e.getPhone());
        r.setRole(e.getRole() != null ? e.getRole().name() : null);
        r.setBranchName(e.getBranch() != null ? e.getBranch().getName() : null);
        r.setBranchId(e.getBranch() != null ? e.getBranch().getId() : null);
        r.setIsActive(e.getIsActive());
        r.setIsAccountApproved(e.getIsAccountApproved());
        r.setIsOwner(e.getIsOwner());
        r.setWageType(e.getWageType() != null ? e.getWageType().name() : null);
        r.setDailyRate(e.getDailyRate());
        r.setHourlyWage(e.getHourlyWage());
        r.setTotalLeaveDaysPerYear(e.getTotalLeaveDaysPerYear());
        r.setRemainingLeaveDays(e.getRemainingLeaveDays());
        r.setCarryOverLeave(e.getCarryOverLeave());
        r.setMaxAdvanceLimit(e.getMaxAdvanceLimit());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
