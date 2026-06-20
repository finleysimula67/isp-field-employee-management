package com.workflow.service;

import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final EmployeeRepository employeeRepository;
    private final DailyLogRepository dailyLogRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final BranchRepository branchRepository;
    private final TaskRepository taskRepository;

    public DashboardService(EmployeeRepository er, DailyLogRepository dlr,
                            LeaveRequestRepository lrr, BranchRepository br, TaskRepository tr) {
        this.employeeRepository = er;
        this.dailyLogRepository = dlr;
        this.leaveRequestRepository = lrr;
        this.branchRepository = br;
        this.taskRepository = tr;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("activeToday", dailyLogRepository.countByLogDate(LocalDate.now()));
        stats.put("pendingApprovals", dailyLogRepository.countByStatus(LogStatus.PENDING));
        stats.put("pendingLeaveRequests", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        stats.put("totalBranches", branchRepository.count());
        stats.put("openTasks", taskRepository.countByStatus(TaskStatus.OPEN));
        return stats;
    }

    public Map<String, Object> getEmployeeStats(Long employeeId) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        long todayCount = dailyLogRepository.countByEmployeeIdAndLogDate(employeeId, today);
        stats.put("todayLog", todayCount > 0 ? "SUBMITTED" : null);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        BigDecimal weekHours = dailyLogRepository.findByEmployeeIdAndLogDateBetween(employeeId, weekStart, weekEnd)
                .stream().filter(l -> l.getHoursWorked() != null)
                .map(DailyLog::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("weekHours", weekHours);
        stats.put("remainingLeaveDays", employeeRepository.findById(employeeId)
                .map(Employee::getRemainingLeaveDays).orElse(BigDecimal.ZERO));
        long pendingTasks = taskRepository.countByAssignedToIdAndStatus(employeeId, TaskStatus.OPEN)
                + taskRepository.countByAssignedToIdAndStatus(employeeId, TaskStatus.IN_PROGRESS);
        stats.put("pendingTasks", pendingTasks);
        return stats;
    }
}
