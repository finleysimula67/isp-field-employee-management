package com.workflow.service;

import com.workflow.dto.DailyLogRequest;
import com.workflow.dto.DailyLogReviewRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DailyLogService {
    private static final Logger log = LoggerFactory.getLogger(DailyLogService.class);
    private final DailyLogRepository dailyLogRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final PayrollRecordRepository payrollRecordRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final RecycleBinService recycleBinService;

    public DailyLogService(DailyLogRepository dlr, EmployeeRepository er, BranchRepository br,
                           NotificationRepository nr, AuditLogService als,
                           PayrollRecordRepository prr, EmailService emailService,
                           NotificationService notificationService, RecycleBinService rbs) {
        this.dailyLogRepository = dlr; this.employeeRepository = er; this.branchRepository = br;
        this.notificationRepository = nr; this.auditLogService = als; this.payrollRecordRepository = prr;
        this.emailService = emailService; this.notificationService = notificationService;
        this.recycleBinService = rbs;
    }

    public List<DailyLog> getDailyLogs(Long employeeId, String status, LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "submittedAt");
        LogStatus statusEnum = status != null ? LogStatus.valueOf(status) : null;
        return dailyLogRepository.findFiltered(employeeId, statusEnum, date, pageable).getContent();
    }

    public DailyLog getDailyLog(Long id) {
        return dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Daily log not found"));
    }

    @Transactional
    public DailyLog createDailyLog(DailyLogRequest request, Long currentUserId) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        DailyLog log = new DailyLog();
        log.setEmployee(employee);
        if (request.getBranchId() != null)
            branchRepository.findById(request.getBranchId()).ifPresent(log::setBranch);
        LocalDate logDate = request.getLogDate() != null ? LocalDate.parse(request.getLogDate()) : LocalDate.now();
        if (logDate.isAfter(LocalDate.now()))
            throw new RuntimeException("Log date cannot be in the future");
        log.setLogDate(logDate);
        if (request.getStartTime() != null) log.setStartTime(LocalTime.parse(request.getStartTime()));
        if (request.getEndTime() != null) log.setEndTime(LocalTime.parse(request.getEndTime()));
        if (log.getStartTime() != null && log.getEndTime() != null && log.getEndTime().isBefore(log.getStartTime()))
            throw new RuntimeException("End time must be after start time");
        if (request.getHoursWorked() != null) {
            log.setHoursWorked(request.getHoursWorked());
        } else if (log.getStartTime() != null && log.getEndTime() != null) {
            long mins = ChronoUnit.MINUTES.between(log.getStartTime(), log.getEndTime());
            log.setHoursWorked(BigDecimal.valueOf(mins / 60.0).setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (request.getCategory() != null) log.setCategory(LogCategory.valueOf(request.getCategory()));
        if (request.getLocationDescription() != null) log.setLocationDescription(request.getLocationDescription());
        if (request.getLocationLat() != null) log.setLocationLat(BigDecimal.valueOf(request.getLocationLat()));
        if (request.getLocationLng() != null) log.setLocationLng(BigDecimal.valueOf(request.getLocationLng()));
        log.setWorkDescription(request.getWorkDescription());
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            log.setPhotoUrls(String.join(",", request.getPhotoUrls()));
        }
        if (request.getAssignedTaskId() != null) log.setAssignedTaskId(request.getAssignedTaskId());

        logDate = log.getLogDate();
        boolean hasApprovedDuplicate = dailyLogRepository.findByEmployeeAndLogDateBetween(employee, logDate, logDate)
                .stream().anyMatch(l -> l.getStatus() == LogStatus.APPROVED && !l.getId().equals(log.getId()));

        if (hasApprovedDuplicate) {
            log.setStatus(LogStatus.REJECTED);
            log.setReviewComment("Auto-rejected: Duplicate entry - employee already has an approved log for " + logDate);
        } else {
            log.setStatus(LogStatus.PENDING);
        }

        DailyLog saved = dailyLogRepository.save(log);
        auditLogService.log("DailyLog", saved.getId(), "CREATED", null, saved.getStatus().name(), employee.getEmail());

        List<Employee> admins = employeeRepository.findByRoleIn(
                List.of(Role.SUPER_ADMIN, Role.BRANCH_MANAGER));
        for (Employee admin : admins) {
            if (admin.getId().equals(employee.getId())) continue;
            Notification notif = new Notification();
            notif.setRecipient(admin);
            notif.setType("DAILY_LOG_SUBMITTED");
            notif.setTitle("New Daily Log from " + employee.getName());
            notif.setBody(request.getWorkDescription());
            notif.setRelatedEntityType("DailyLog");
            notif.setRelatedEntityId(saved.getId());
            notificationRepository.save(notif);
            notificationService.broadcastNotificationToRecipient(notif);

            // 🛡️ Safely handle email notification submission
            try {
                emailService.sendEmail(admin.getEmail(), "New Daily Log: " + employee.getName(),
                        employee.getName() + " submitted a daily log for " + log.getLogDate()
                        + ".\n\nDescription: " + (request.getWorkDescription() != null ? request.getWorkDescription() : "N/A"));
            } catch (Exception e) {
                System.err.println("Outbound notification email skipped: Network or mail server unreachable.");
            }
        }

        return saved;
    }

    @Transactional
    public DailyLog reviewDailyLog(Long id, DailyLogReviewRequest request, Long reviewerId) {
        DailyLog log = dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Daily log not found"));
        if (log.getStatus() != LogStatus.PENDING)
            throw new RuntimeException("Daily log is not in PENDING status");
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        LogStatus newStatus = LogStatus.valueOf(request.getStatus());
        log.setStatus(newStatus);
        log.setReviewComment(request.getReviewComment());
        log.setReviewedBy(reviewer);
        log.setReviewedAt(LocalDateTime.now());
        if (newStatus == LogStatus.REJECTED || newStatus == LogStatus.NEEDS_REVISION) {
            Notification notification = new Notification();
            notification.setRecipient(log.getEmployee());
            notification.setType("DAILY_LOG_REVIEW");
            notification.setTitle("Daily Log " + newStatus.name());
            notification.setBody(request.getReviewComment());
            notification.setRelatedEntityType("DailyLog");
            notification.setRelatedEntityId(log.getId());
            notificationRepository.save(notification);
            notificationService.broadcastNotificationToRecipient(notification);
        }
        DailyLog saved = dailyLogRepository.save(log);
        auditLogService.log("DailyLog", id, "REVIEWED", "PENDING", newStatus.name(), reviewer.getEmail());

        if (newStatus == LogStatus.APPROVED) {
            Employee emp = log.getEmployee();
            LocalDate logDate = log.getLogDate() != null ? log.getLogDate() : LocalDate.now();
            LocalDate periodStart = logDate.withDayOfMonth(1);
            LocalDate periodEnd = logDate.withDayOfMonth(logDate.lengthOfMonth());
            String periodLabel = periodStart.getMonthValue() + "/" + periodStart.getYear();

            List<PayrollRecord> existing = payrollRecordRepository
                    .findByEmployeeIdOrderByPeriodStartDesc(emp.getId());
            PayrollRecord payroll = existing.stream()
                    .filter(r -> r.getPeriodLabel().equals(periodLabel)
                            && (r.getStatus() == PayrollStatus.DRAFT
                                    || r.getStatus() == PayrollStatus.CALCULATED))
                    .findFirst().orElse(null);

            if (payroll == null) {
                payroll = new PayrollRecord();
                payroll.setEmployee(emp);
                payroll.setPeriodLabel(periodLabel);
                payroll.setPeriodStart(periodStart);
                payroll.setPeriodEnd(periodEnd);
                payroll.setDaysWorked(0);
                payroll.setTotalHours(BigDecimal.ZERO);
                payroll.setGrossPay(BigDecimal.ZERO);
                payroll.setNetPay(BigDecimal.ZERO);
                payroll.setDeductions(BigDecimal.ZERO);
                payroll.setOvertimeHours(BigDecimal.ZERO);
                payroll.setStatus(PayrollStatus.CALCULATED);
            }

            List<DailyLog> approvedLogs = dailyLogRepository.findByEmployeeAndLogDateBetween(emp, periodStart, periodEnd)
                    .stream().filter(l -> l.getStatus() == LogStatus.APPROVED).collect(Collectors.toList());
            Set<LocalDate> uniqueDates = approvedLogs.stream().map(DailyLog::getLogDate).collect(Collectors.toSet());
            int daysWorked = uniqueDates.size();
            BigDecimal totalHours = approvedLogs.stream()
                    .filter(l -> l.getHoursWorked() != null)
                    .map(DailyLog::getHoursWorked)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            payroll.setDaysWorked(daysWorked);
            payroll.setTotalHours(totalHours);

            BigDecimal wageRate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
            payroll.setWageRateAtTime(wageRate);

            if (emp.getWageType() == WageType.HOURLY) {
                payroll.setGrossPay(wageRate.multiply(totalHours));
                payroll.setNetPay(wageRate.multiply(totalHours));
            } else {
                BigDecimal gross = wageRate.multiply(BigDecimal.valueOf(daysWorked));
                payroll.setGrossPay(gross);
                payroll.setNetPay(gross);
            }
            payrollRecordRepository.save(payroll);
            auditLogService.log("PayrollRecord", payroll.getId(), "AUTO_CALCULATED",
                    null, payroll.getStatus().name(), emp.getEmail());

            Notification notif = new Notification();
            notif.setRecipient(emp);
            notif.setType("DAILY_LOG_APPROVED");
            notif.setTitle("Daily Log Approved");
            notif.setBody("Approved for " + logDate + " (" + periodLabel + ")");
            notif.setRelatedEntityType("DailyLog");
            notif.setRelatedEntityId(saved.getId());
            notificationRepository.save(notif);
            notificationService.broadcastNotificationToRecipient(notif);
            try {
                emailService.sendEmail(emp.getEmail(), "Daily Log Approved",
                        "Your daily log for " + logDate + " has been approved.");
            } catch (Exception e) {
                System.err.println("Approval confirmation email skipped: Network or mail server unreachable.");
            }
        }

        return saved;
    }

    public List<DailyLog> batchReviewDailyLogs(List<Long> ids, DailyLogReviewRequest request, Long reviewerId) {
        List<DailyLog> logs = dailyLogRepository.findByIdIn(ids);
        for (DailyLog log : logs) {
            reviewDailyLog(log.getId(), request, reviewerId);
        }
        return dailyLogRepository.findByIdIn(ids);
    }

    public List<DailyLog> getMyLogs(Long employeeId, String category, LocalDate date) {
        List<DailyLog> logs = dailyLogRepository.findByEmployeeIdWithEager(employeeId);
        if (category != null)
            logs = logs.stream().filter(l -> l.getCategory().name().equals(category)).collect(Collectors.toList());
        if (date != null)
            logs = logs.stream().filter(l -> l.getLogDate().equals(date)).collect(Collectors.toList());
        return logs;
    }

    @Transactional
    public void softDeleteDailyLog(Long id, Employee actor) {
        DailyLog log = dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Daily log not found"));
        boolean isOwner = log.getEmployee().getId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == Role.SUPER_ADMIN || actor.getRole() == Role.BRANCH_MANAGER;
        if (!isAdmin && (!isOwner || log.getStatus() != LogStatus.PENDING))
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to delete this log");
        log.setDeletedAt(java.time.LocalDateTime.now());
        log.setDeletedBy(actor.getId());
        dailyLogRepository.save(log);
        recycleBinService.softDelete(log, id, "DailyLog", actor, log.getEmployee().getId(), log.getSubmittedAt());
        auditLogService.log("DailyLog", id, "SOFT_DELETED", log.getStatus().name(), "DELETED", actor.getEmail());
    }

    @Transactional
    public void batchDeleteDailyLogs(List<Long> ids, Employee actor) {
        int successCount = 0;
        for (Long id : ids) {
            try { softDeleteDailyLog(id, actor); successCount++; }
            catch (Exception e) { log.warn("Batch delete failed for DailyLog id {}: {}", id, e.getMessage()); }
        }
        recycleBinService.bulkDeleteLogged("DailyLog", successCount, actor);
    }

    public Map<String, Object> getEarningsSummary(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<DailyLog> allLogs = dailyLogRepository.findByEmployeeIdOrderByLogDateDesc(employeeId);
        List<DailyLog> approved = allLogs.stream()
                .filter(l -> l.getStatus() == LogStatus.APPROVED)
                .collect(Collectors.toList());
        long approvedDays = approved.stream()
                .map(DailyLog::getLogDate)
                .distinct()
                .count();
        BigDecimal totalHours = approved.stream()
                .filter(l -> l.getHoursWorked() != null)
                .map(DailyLog::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
        BigDecimal totalEarned;
        if (emp.getWageType() == WageType.HOURLY) {
            BigDecimal hourlyRate = emp.getHourlyWage() != null ? emp.getHourlyWage() : BigDecimal.valueOf(100);
            totalEarned = hourlyRate.multiply(totalHours);
        } else {
            totalEarned = rate.multiply(BigDecimal.valueOf(approvedDays));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("approvedDays", approvedDays);
        result.put("totalHours", totalHours);
        result.put("dailyRate", rate);
        result.put("totalEarned", totalEarned);
        return result;
    }
}
