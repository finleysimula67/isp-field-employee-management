package com.workflow.service;

import com.workflow.dto.DailyLogRequest;
import com.workflow.dto.DailyLogReviewRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyLogService {
    private final DailyLogRepository dailyLogRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final PayrollRecordRepository payrollRecordRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public DailyLogService(DailyLogRepository dlr, EmployeeRepository er, BranchRepository br,
                           NotificationRepository nr, AuditLogService als,
                           PayrollRecordRepository prr, EmailService emailService,
                           NotificationService notificationService) {
        this.dailyLogRepository = dlr; this.employeeRepository = er; this.branchRepository = br;
        this.notificationRepository = nr; this.auditLogService = als; this.payrollRecordRepository = prr;
        this.emailService = emailService; this.notificationService = notificationService;
    }

    public List<DailyLog> getDailyLogs(Long employeeId, String status, LocalDate date) {
        List<DailyLog> logs = dailyLogRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"));
        if (employeeId != null)
            logs = logs.stream().filter(l -> l.getEmployee().getId().equals(employeeId)).collect(Collectors.toList());
        if (status != null)
            logs = logs.stream().filter(l -> l.getStatus().name().equals(status)).collect(Collectors.toList());
        if (date != null)
            logs = logs.stream().filter(l -> l.getLogDate().equals(date)).collect(Collectors.toList());
        return logs;
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
        log.setLogDate(request.getLogDate() != null ? LocalDate.parse(request.getLogDate()) : LocalDate.now());
        if (request.getStartTime() != null) log.setStartTime(LocalTime.parse(request.getStartTime()));
        if (request.getEndTime() != null) log.setEndTime(LocalTime.parse(request.getEndTime()));
        if (request.getHoursWorked() != null) log.setHoursWorked(BigDecimal.valueOf(request.getHoursWorked()));
        if (request.getCategory() != null) log.setCategory(LogCategory.valueOf(request.getCategory()));
        if (request.getLocationDescription() != null) log.setLocationDescription(request.getLocationDescription());
        if (request.getLocationLat() != null) log.setLocationLat(BigDecimal.valueOf(request.getLocationLat()));
        if (request.getLocationLng() != null) log.setLocationLng(BigDecimal.valueOf(request.getLocationLng()));
        log.setWorkDescription(request.getWorkDescription());
        log.setPhotoUrls(request.getPhotoUrls());
        if (request.getAssignedTaskId() != null) log.setAssignedTaskId(request.getAssignedTaskId());
        log.setStatus(LogStatus.PENDING);
        DailyLog saved = dailyLogRepository.save(log);
        auditLogService.log("DailyLog", saved.getId(), "CREATED", null, "PENDING", employee.getEmail());

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
            emailService.sendEmail(admin.getEmail(), "New Daily Log: " + employee.getName(),
                    employee.getName() + " submitted a daily log for " + log.getLogDate()
                    + ".\n\nDescription: " + (request.getWorkDescription() != null ? request.getWorkDescription() : "N/A"));
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

            payroll.setDaysWorked(payroll.getDaysWorked() + 1);
            if (log.getHoursWorked() != null) {
                payroll.setTotalHours(payroll.getTotalHours().add(log.getHoursWorked()));
            }

            BigDecimal wageRate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
            payroll.setWageRateAtTime(wageRate);

            BigDecimal addition;
            if (emp.getWageType() == WageType.HOURLY && log.getHoursWorked() != null) {
                addition = wageRate.multiply(log.getHoursWorked());
            } else {
                addition = wageRate;
            }
            payroll.setGrossPay(payroll.getGrossPay().add(addition));
            payroll.setNetPay(payroll.getNetPay().add(addition));
            payrollRecordRepository.save(payroll);
            auditLogService.log("PayrollRecord", payroll.getId(), "AUTO_CALCULATED",
                    null, payroll.getStatus().name(), emp.getEmail());

            Notification notif = new Notification();
            notif.setRecipient(emp);
            notif.setType("DAILY_LOG_APPROVED");
            notif.setTitle("Daily Log Approved");
            notif.setBody("Rs. " + addition + " credited for " + logDate + " (" + periodLabel + ")");
            notif.setRelatedEntityType("DailyLog");
            notif.setRelatedEntityId(saved.getId());
            notificationRepository.save(notif);
            notificationService.broadcastNotificationToRecipient(notif);
            emailService.sendEmail(emp.getEmail(), "Daily Log Approved",
                    "Your daily log for " + logDate + " has been approved.\n\n"
                    + "Rs. " + addition + " credited (" + periodLabel + ").");
        }

        return saved;
    }

    @Transactional
    public List<DailyLog> batchReviewDailyLogs(List<Long> ids, DailyLogReviewRequest request, Long reviewerId) {
        List<DailyLog> logs = dailyLogRepository.findByIdIn(ids);
        for (DailyLog log : logs) {
            reviewDailyLog(log.getId(), request, reviewerId);
        }
        return dailyLogRepository.findByIdIn(ids);
    }

    public List<DailyLog> getMyLogs(Long employeeId, String category, LocalDate date) {
        List<DailyLog> logs = dailyLogRepository.findByEmployeeIdOrderByLogDateDesc(employeeId);
        if (category != null)
            logs = logs.stream().filter(l -> l.getCategory().name().equals(category)).collect(Collectors.toList());
        if (date != null)
            logs = logs.stream().filter(l -> l.getLogDate().equals(date)).collect(Collectors.toList());
        return logs;
    }

    public Map<String, Object> getEarningsSummary(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        long approvedDays = dailyLogRepository.countByEmployeeIdAndStatus(employeeId, LogStatus.APPROVED);
        BigDecimal dailyRate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
        BigDecimal totalEarned = dailyRate.multiply(BigDecimal.valueOf(approvedDays));
        Map<String, Object> result = new HashMap<>();
        result.put("approvedDays", approvedDays);
        result.put("dailyRate", dailyRate);
        result.put("totalEarned", totalEarned);
        return result;
    }
}
