package com.workflow.service;

import com.workflow.dto.AdvanceReviewRequest;
import com.workflow.dto.ManualAdvanceRequest;
import com.workflow.dto.SalaryAdvanceRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalaryAdvanceService {
    private final SalaryAdvanceRepository salaryAdvanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DailyLogRepository dailyLogRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final RecycleBinService recycleBinService;

    public SalaryAdvanceService(SalaryAdvanceRepository sar, EmployeeRepository er,
                                DailyLogRepository dlr, PayrollRecordRepository prr,
                                NotificationRepository nr, AuditLogService als,
                                EmailService emailService, NotificationService notificationService,
                                RecycleBinService rbs) {
        this.salaryAdvanceRepository = sar; this.employeeRepository = er;
        this.dailyLogRepository = dlr; this.payrollRecordRepository = prr;
        this.notificationRepository = nr; this.auditLogService = als;
        this.emailService = emailService; this.notificationService = notificationService;
        this.recycleBinService = rbs;
    }

    public List<SalaryAdvance> getAdvances(Long employeeId, String status) {
        List<SalaryAdvance> advances;
        if (employeeId != null) {
            advances = salaryAdvanceRepository.findByEmployeeIdOrderByRequestDateDesc(employeeId);
        } else {
            advances = salaryAdvanceRepository.findAll(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "requestDate"));
        }
        if (status != null)
            advances = advances.stream().filter(a -> a.getStatus().name().equals(status)).collect(Collectors.toList());
        return advances;
    }

    public SalaryAdvance getAdvance(Long id) {
        return salaryAdvanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary advance not found"));
    }

    @Transactional
    public SalaryAdvance requestAdvance(SalaryAdvanceRequest request, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Map<String, BigDecimal> balance = getEmployeeBalance(employeeId);
        BigDecimal available = balance.get("availableForAdvance");
        if (BigDecimal.valueOf(request.getAmount()).compareTo(available) > 0) {
            throw new RuntimeException("Advance request of Rs. " + String.format("%.0f", request.getAmount())
                    + " exceeds available limit of Rs. " + String.format("%.0f", available)
                    + " (earned Rs. " + String.format("%.0f", balance.get("totalEarned"))
                    + " minus advanced Rs. " + String.format("%.0f", balance.get("totalAdvanced")) + ")");
        }
        SalaryAdvance advance = new SalaryAdvance();
        advance.setEmployee(employee);
        advance.setAmount(BigDecimal.valueOf(request.getAmount()));
        advance.setRequestDate(LocalDate.now());
        advance.setReason(request.getReason());
        advance.setStatus(AdvanceStatus.PENDING);
        SalaryAdvance saved = salaryAdvanceRepository.save(advance);
        auditLogService.log("SalaryAdvance", saved.getId(), "REQUESTED", null, "PENDING", employee.getEmail());

        List<Employee> admins = employeeRepository.findByRoleIn(
                List.of(Role.SUPER_ADMIN, Role.BRANCH_MANAGER));
        for (Employee admin : admins) {
            if (admin.getId().equals(employee.getId())) continue;
            Notification adminNotif = new Notification();
            adminNotif.setRecipient(admin);
            adminNotif.setType("ADVANCE_REQUESTED");
            adminNotif.setTitle("Advance Request: " + employee.getName());
            adminNotif.setBody("Rs. " + String.format("%.0f", request.getAmount())
                    + (request.getReason() != null ? " - " + request.getReason() : ""));
            adminNotif.setRelatedEntityType("SalaryAdvance");
            adminNotif.setRelatedEntityId(saved.getId());
            notificationRepository.save(adminNotif);
            notificationService.broadcastNotificationToRecipient(adminNotif);
            try {
                emailService.sendEmail(admin.getEmail(), "New Salary Advance: " + employee.getName(),
                        employee.getName() + " requested a salary advance of Rs. "
                        + String.format("%.0f", request.getAmount())
                        + ".\n\nReason: " + (request.getReason() != null ? request.getReason() : "N/A"));
            } catch (Exception e) {
                System.err.println("Advance request notification email skipped: " + e.getMessage());
            }
        }

        return saved;
    }

    @Transactional
    public SalaryAdvance reviewAdvance(Long id, AdvanceReviewRequest request, Long reviewerId) {
        SalaryAdvance advance = salaryAdvanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary advance not found"));
        if (advance.getStatus() != AdvanceStatus.PENDING)
            throw new RuntimeException("Advance is not in PENDING status");
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        AdvanceStatus newStatus = AdvanceStatus.valueOf(request.getStatus());
        advance.setStatus(newStatus);
        advance.setApprovedBy(reviewer);
        advance.setApprovedAt(LocalDateTime.now());
        advance.setNotes(request.getNotes());
        if (newStatus == AdvanceStatus.APPROVED) {
            advance.setDisbursedAt(LocalDateTime.now());
        }
        Notification notification = new Notification();
        notification.setRecipient(advance.getEmployee());
        notification.setType("ADVANCE_REVIEW");
        notification.setTitle("Salary Advance " + newStatus.name());
        notification.setBody(request.getNotes());
        notification.setRelatedEntityType("SalaryAdvance");
        notification.setRelatedEntityId(advance.getId());
        notificationRepository.save(notification);
        notificationService.broadcastNotificationToRecipient(notification);
        try {
            emailService.sendEmail(advance.getEmployee().getEmail(), "Salary Advance " + newStatus.name(),
                    "Your salary advance of Rs. " + String.format("%.0f", advance.getAmount())
                    + " has been " + newStatus.name() + ".\n\nNotes: "
                    + (request.getNotes() != null ? request.getNotes() : "N/A"));
        } catch (Exception e) {
            System.err.println("Advance review notification email skipped: " + e.getMessage());
        }
        SalaryAdvance saved = salaryAdvanceRepository.save(advance);
        auditLogService.log("SalaryAdvance", id, "REVIEWED", "PENDING", newStatus.name(), reviewer.getEmail());
        return saved;
    }

    @Transactional
    public SalaryAdvance createManualAdvance(ManualAdvanceRequest request, Long createdById) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Employee createdBy = employeeRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        Map<String, BigDecimal> balance = getEmployeeBalance(employee.getId());
        BigDecimal available = balance.get("availableForAdvance");
        if (BigDecimal.valueOf(request.getAmount()).compareTo(available) > 0) {
            throw new RuntimeException("Manual advance of Rs. " + String.format("%.0f", request.getAmount())
                    + " exceeds available limit of Rs. " + String.format("%.0f", available)
                    + " for " + employee.getName());
        }
        SalaryAdvance advance = new SalaryAdvance();
        advance.setEmployee(employee);
        advance.setAmount(BigDecimal.valueOf(request.getAmount()));
        advance.setRequestDate(LocalDate.now());
        advance.setReason(request.getReason());
        advance.setStatus(AdvanceStatus.DISBURSED);
        advance.setApprovedBy(createdBy);
        advance.setApprovedAt(LocalDateTime.now());
        advance.setDisbursedAt(LocalDateTime.now());
        SalaryAdvance saved = salaryAdvanceRepository.save(advance);
        Notification notification = new Notification();
        notification.setRecipient(employee);
        notification.setType("ADVANCE_MANUAL");
        notification.setTitle("Advance Disbursed: Rs. " + String.format("%.0f", request.getAmount()));
        notification.setBody("Manual advance of Rs. " + String.format("%.0f", request.getAmount())
                + " has been added by " + createdBy.getName() + (request.getReason() != null ? ". Reason: " + request.getReason() : ""));
        notification.setRelatedEntityType("SalaryAdvance");
        notification.setRelatedEntityId(saved.getId());
        notificationRepository.save(notification);
        notificationService.broadcastNotificationToRecipient(notification);
        try {
            emailService.sendEmail(employee.getEmail(), "Advance Disbursed",
                    "A manual salary advance of Rs. " + String.format("%.0f", request.getAmount())
                    + " has been disbursed by " + createdBy.getName() + "."
                    + (request.getReason() != null ? "\n\nReason: " + request.getReason() : ""));
        } catch (Exception e) {
            System.err.println("Manual advance notification email skipped: " + e.getMessage());
        }
        auditLogService.log("SalaryAdvance", saved.getId(), "MANUAL_CREATED", null, "DISBURSED", employee.getEmail());
        return saved;
    }

    @Transactional
    public void softDeleteSalaryAdvance(Long id, Employee actor) {
        SalaryAdvance adv = salaryAdvanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary advance not found"));
        boolean isAdmin = actor.getRole() == Role.SUPER_ADMIN || actor.getRole() == Role.BRANCH_MANAGER;
        if (!isAdmin)
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to delete this advance");
        adv.setDeletedAt(java.time.LocalDateTime.now());
        adv.setDeletedBy(actor.getId());
        salaryAdvanceRepository.save(adv);
        recycleBinService.softDelete(adv, id, "SalaryAdvance", actor, adv.getEmployee().getId(), adv.getRequestDate().atStartOfDay());
        auditLogService.log("SalaryAdvance", id, "SOFT_DELETED", adv.getStatus().name(), "DELETED", actor.getEmail());
    }

    @Transactional
    public void batchDeleteSalaryAdvances(List<Long> ids, Employee actor) {
        for (Long id : ids) { try { softDeleteSalaryAdvance(id, actor); } catch (Exception e) { /* skip */ } }
        recycleBinService.bulkDeleteLogged("SalaryAdvance", ids.size(), actor);
    }

    public Map<String, BigDecimal> getEmployeeBalance(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        long approvedDays = dailyLogRepository.findByEmployeeIdOrderByLogDateDesc(employeeId).stream()
                .filter(l -> l.getStatus() == LogStatus.APPROVED)
                .map(DailyLog::getLogDate)
                .distinct()
                .count();
        BigDecimal dailyRate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
        BigDecimal totalEarned = dailyRate.multiply(BigDecimal.valueOf(approvedDays));

        List<SalaryAdvance> advances = salaryAdvanceRepository.findByEmployeeIdOrderByRequestDateDesc(employeeId);
        BigDecimal totalDrawn = advances.stream()
                .filter(a -> (a.getStatus() == AdvanceStatus.APPROVED || a.getStatus() == AdvanceStatus.DISBURSED)
                        && !a.getIsSettled())
                .map(SalaryAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSettled = advances.stream()
                .filter(a -> a.getIsSettled())
                .map(SalaryAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDisbursed = advances.stream()
                .filter(a -> a.getStatus() == AdvanceStatus.DISBURSED)
                .map(SalaryAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAdvanced = advances.stream()
                .filter(a -> (a.getStatus() == AdvanceStatus.APPROVED || a.getStatus() == AdvanceStatus.DISBURSED)
                        && !a.getIsSettled())
                .map(SalaryAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal advanceLimit = emp.getMaxAdvanceLimit() != null ? emp.getMaxAdvanceLimit() : BigDecimal.valueOf(5000);
        BigDecimal availableForAdvance = advanceLimit.subtract(totalAdvanced);
        if (availableForAdvance.compareTo(BigDecimal.ZERO) < 0) availableForAdvance = BigDecimal.ZERO;

        Map<String, BigDecimal> balance = new HashMap<>();
        balance.put("totalEarned", totalEarned);
        balance.put("totalDrawn", totalDrawn);
        balance.put("totalSettled", totalSettled);
        balance.put("totalDisbursed", totalDisbursed);
        balance.put("totalAdvanced", totalAdvanced);
        balance.put("maxAdvanceLimit", advanceLimit);
        balance.put("availableForAdvance", availableForAdvance);
        return balance;
    }
}
