package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final EmployeeRepository employeeRepository;
    private final DailyLogRepository dailyLogRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final BranchRepository branchRepository;
    private final TaskRepository taskRepository;

    public DashboardController(EmployeeRepository er, DailyLogRepository dlr,
                               LeaveRequestRepository lrr, BranchRepository br, TaskRepository tr) {
        this.employeeRepository = er; this.dailyLogRepository = dlr;
        this.leaveRequestRepository = lrr; this.branchRepository = br; this.taskRepository = tr;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("activeToday", dailyLogRepository.countByLogDate(LocalDate.now()));
        stats.put("pendingApprovals", dailyLogRepository.countByStatus(LogStatus.PENDING));
        stats.put("pendingLeaveRequests", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        stats.put("totalBranches", branchRepository.count());
        stats.put("openTasks", taskRepository.countByStatus(TaskStatus.OPEN));
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/employee-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeStats(
            @AuthenticationPrincipal Employee employee) {
        Map<String, Object> stats = new HashMap<>();
        if (employee == null) return ResponseEntity.ok(ApiResponse.ok(stats));
        LocalDate today = LocalDate.now();
        long todayCount = dailyLogRepository.countByEmployeeIdAndLogDate(employee.getId(), today);
        stats.put("todayLog", todayCount > 0 ? "SUBMITTED" : null);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        BigDecimal weekHours = dailyLogRepository.findByEmployeeIdAndLogDateBetween(employee.getId(), weekStart, weekEnd)
                .stream().filter(l -> l.getHoursWorked() != null)
                .map(DailyLog::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("weekHours", weekHours);
        stats.put("remainingLeaveDays", employee.getRemainingLeaveDays());
        long pendingTasks = taskRepository.countByAssignedToIdAndStatus(employee.getId(), TaskStatus.OPEN)
                + taskRepository.countByAssignedToIdAndStatus(employee.getId(), TaskStatus.IN_PROGRESS);
        stats.put("pendingTasks", pendingTasks);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/employee-stats/{empId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeStatsById(@PathVariable Long empId) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        long todayCount = dailyLogRepository.countByEmployeeIdAndLogDate(empId, today);
        stats.put("todayLog", todayCount > 0 ? "SUBMITTED" : null);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        BigDecimal weekHours = dailyLogRepository.findByEmployeeIdAndLogDateBetween(empId, weekStart, weekEnd)
                .stream().filter(l -> l.getHoursWorked() != null)
                .map(DailyLog::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("weekHours", weekHours);
        stats.put("remainingLeaveDays", employeeRepository.findById(empId)
                .map(Employee::getRemainingLeaveDays).orElse(BigDecimal.ZERO));
        long pendingTasks = taskRepository.countByAssignedToIdAndStatus(empId, TaskStatus.OPEN)
                + taskRepository.countByAssignedToIdAndStatus(empId, TaskStatus.IN_PROGRESS);
        stats.put("pendingTasks", pendingTasks);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
