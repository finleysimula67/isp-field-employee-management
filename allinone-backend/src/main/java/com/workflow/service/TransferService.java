package com.workflow.service;

import com.workflow.dto.TransferResult;
import com.workflow.entity.AuditLog;
import com.workflow.entity.Employee;
import com.workflow.entity.TransferLog;
import com.workflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TransferService {

    private final EmployeeRepository employeeRepository;
    private final DailyLogRepository dailyLogRepository;
    private final CashCollectionRepository cashCollectionRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TaskRepository taskRepository;
    private final SalaryAdvanceRepository salaryAdvanceRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final MonthlyLockoutRepository monthlyLockoutRepository;
    private final RecycleBinRepository recycleBinRepository;
    private final BranchRepository branchRepository;
    private final HolidayRepository holidayRepository;
    private final EmailAllowListRepository emailAllowListRepository;
    private final TransferLogRepository transferLogRepository;

    public TransferService(EmployeeRepository employeeRepository,
                           DailyLogRepository dailyLogRepository,
                           CashCollectionRepository cashCollectionRepository,
                           LeaveRequestRepository leaveRequestRepository,
                           TaskRepository taskRepository,
                           SalaryAdvanceRepository salaryAdvanceRepository,
                           PayrollRecordRepository payrollRecordRepository,
                           NotificationRepository notificationRepository,
                           AuditLogRepository auditLogRepository,
                           MonthlyLockoutRepository monthlyLockoutRepository,
                           RecycleBinRepository recycleBinRepository,
                           BranchRepository branchRepository,
                           HolidayRepository holidayRepository,
                           EmailAllowListRepository emailAllowListRepository,
                           TransferLogRepository transferLogRepository) {
        this.employeeRepository = employeeRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.cashCollectionRepository = cashCollectionRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.taskRepository = taskRepository;
        this.salaryAdvanceRepository = salaryAdvanceRepository;
        this.payrollRecordRepository = payrollRecordRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.monthlyLockoutRepository = monthlyLockoutRepository;
        this.recycleBinRepository = recycleBinRepository;
        this.branchRepository = branchRepository;
        this.holidayRepository = holidayRepository;
        this.emailAllowListRepository = emailAllowListRepository;
        this.transferLogRepository = transferLogRepository;
    }

    public TransferResult transferAllData(Long sourceId, Long targetId, boolean deleteSource, Long performedBy) {
        if (sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Source and target employees must be different");
        }

        Employee source = employeeRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source employee not found with id: " + sourceId));
        Employee target = employeeRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target employee not found with id: " + targetId));

        if (Boolean.FALSE.equals(source.getIsActive())) {
            throw new IllegalArgumentException("Source employee is not active");
        }
        if (Boolean.FALSE.equals(target.getIsActive())) {
            throw new IllegalArgumentException("Target employee is not active");
        }

        Map<String, Integer> counts = new LinkedHashMap<>();

        counts.put("dailyLogs_employee", dailyLogRepository.transferEmployeeId(sourceId, targetId));
        counts.put("dailyLogs_reviewedBy", dailyLogRepository.transferReviewedBy(sourceId, targetId));
        counts.put("cashCollections_employee", cashCollectionRepository.transferEmployeeId(sourceId, targetId));
        counts.put("cashCollections_reviewedBy", cashCollectionRepository.transferReviewedBy(sourceId, targetId));
        counts.put("leaveRequests_employee", leaveRequestRepository.transferEmployeeId(sourceId, targetId));
        counts.put("leaveRequests_reviewedBy", leaveRequestRepository.transferReviewedBy(sourceId, targetId));
        counts.put("tasks_assignedTo", taskRepository.transferAssignedTo(sourceId, targetId));
        counts.put("tasks_assignedBy", taskRepository.transferAssignedBy(sourceId, targetId));
        counts.put("salaryAdvances_employee", salaryAdvanceRepository.transferEmployeeId(sourceId, targetId));
        counts.put("salaryAdvances_approvedBy", salaryAdvanceRepository.transferApprovedBy(sourceId, targetId));
        counts.put("payrollRecords_employee", payrollRecordRepository.transferEmployeeId(sourceId, targetId));
        counts.put("payrollRecords_paidBy", payrollRecordRepository.transferPaidBy(sourceId, targetId));
        counts.put("notifications_recipient", notificationRepository.transferRecipientId(sourceId, targetId));
        counts.put("auditLogs_actor", auditLogRepository.transferActorId(sourceId, targetId));
        counts.put("monthlyLockouts_lockedBy", monthlyLockoutRepository.transferLockedBy(sourceId, targetId));
        counts.put("monthlyLockouts_unlockedBy", monthlyLockoutRepository.transferUnlockedBy(sourceId, targetId));
        counts.put("recycleBin_deletedBy", recycleBinRepository.transferDeletedBy(sourceId, targetId));
        counts.put("recycleBin_restoredBy", recycleBinRepository.transferRestoredBy(sourceId, targetId));
        counts.put("branches_manager", branchRepository.clearManagerId(sourceId));
        counts.put("holidays_createdBy", holidayRepository.transferCreatedBy(sourceId, targetId));
        counts.put("emailAllowList_addedBy", emailAllowListRepository.transferAddedBy(sourceId, targetId));

        if (deleteSource) {
            employeeRepository.deleteById(sourceId);
        } else {
            employeeRepository.deactivateAndRemoveOwnership(sourceId);
        }

        int totalTransferred = counts.values().stream().mapToInt(Integer::intValue).sum();

        TransferLog log = new TransferLog();
        log.setSourceEmployeeId(sourceId);
        log.setSourceEmployeeName(source.getName());
        log.setTargetEmployeeId(targetId);
        log.setTargetEmployeeName(target.getName());
        log.setSummaryJson(counts.toString());
        log.setSourceDeleted(deleteSource);
        log.setTransferredAt(LocalDateTime.now());
        log.setTransferredBy(performedBy);
        transferLogRepository.save(log);

        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType("Employee");
        auditLog.setEntityId(sourceId);
        auditLog.setAction(deleteSource ? "TRANSFER_AND_DELETE" : "TRANSFER_AND_DEACTIVATE");
        auditLog.setActor(employeeRepository.getReferenceById(performedBy));
        auditLog.setPreviousStatus("active");
        auditLog.setNewStatus(deleteSource ? "deleted" : "deactivated");
        auditLog.setMetadata("Data transferred to employee " + targetId + " (" + target.getName() +
                "). Total records transferred: " + totalTransferred);
        auditLogRepository.save(auditLog);

        TransferResult result = new TransferResult();
        result.setSourceEmployeeId(sourceId);
        result.setSourceEmployeeName(source.getName());
        result.setTargetEmployeeId(targetId);
        result.setTargetEmployeeName(target.getName());
        result.setTransferredCounts(counts);
        result.setSourceDeleted(deleteSource);
        result.setTotalTransferred(totalTransferred);

        return result;
    }
}
