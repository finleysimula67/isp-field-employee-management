package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.MonthlyLockout;
import com.workflow.service.MonthlyLockoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lockouts")
public class MonthlyLockoutController {
    private final MonthlyLockoutService monthlyLockoutService;

    public MonthlyLockoutController(MonthlyLockoutService monthlyLockoutService) { this.monthlyLockoutService = monthlyLockoutService; }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LockoutStatusResponse>> getStatus(@RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyLockoutService.getLockoutStatus(yearMonth)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LockoutStatusResponse>>> getAll() {
        List<LockoutStatusResponse> list = monthlyLockoutService.getAllLockouts().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/lock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LockoutStatusResponse>> lock(@RequestParam String yearMonth,
                                                                    @AuthenticationPrincipal Employee employee) {
        MonthlyLockout lockout = monthlyLockoutService.lockMonth(yearMonth, employee.getId());
        return ResponseEntity.ok(ApiResponse.ok("Month locked", toResponse(lockout)));
    }

    @PostMapping("/unlock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LockoutStatusResponse>> unlock(@RequestParam String yearMonth,
                                                                      @RequestBody LockoutUnlockRequest request,
                                                                      @AuthenticationPrincipal Employee employee) {
        MonthlyLockout lockout = monthlyLockoutService.unlockMonth(yearMonth, request, employee.getId());
        return ResponseEntity.ok(ApiResponse.ok("Month unlocked", toResponse(lockout)));
    }

    private LockoutStatusResponse toResponse(MonthlyLockout ml) {
        LockoutStatusResponse r = new LockoutStatusResponse();
        r.setId(ml.getId());
        r.setYearMonth(ml.getYearMonth());
        r.setIsLocked(!ml.getIsUnlocked());
        r.setLockedAt(ml.getLockedAt() != null ? ml.getLockedAt().toString() : null);
        r.setLockedBy(ml.getLockedBy() != null ? ml.getLockedBy().getId() : null);
        r.setLockedByName(ml.getLockedBy() != null ? ml.getLockedBy().getName() : null);
        r.setLockDay(ml.getLockDay());
        r.setIsUnlocked(ml.getIsUnlocked());
        r.setUnlockedAt(ml.getUnlockedAt() != null ? ml.getUnlockedAt().toString() : null);
        r.setUnlockedReason(ml.getUnlockReason());
        return r;
    }
}
