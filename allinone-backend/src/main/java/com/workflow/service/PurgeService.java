package com.workflow.service;

import com.workflow.dto.PurgeResult;
import com.workflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class PurgeService {
    private static final Logger log = LoggerFactory.getLogger(PurgeService.class);

    private final RecycleBinRepository recycleBinRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final MonthlyLockoutRepository monthlyLockoutRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final SalaryAdvanceRepository salaryAdvanceRepository;
    private final DailyLogRepository dailyLogRepository;
    private final CashCollectionRepository cashCollectionRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TaskRepository taskRepository;
    private final HolidayRepository holidayRepository;

    public PurgeService(RecycleBinRepository recycleBinRepository,
                        NotificationRepository notificationRepository,
                        AuditLogRepository auditLogRepository,
                        MonthlyLockoutRepository monthlyLockoutRepository,
                        PayrollRecordRepository payrollRecordRepository,
                        SalaryAdvanceRepository salaryAdvanceRepository,
                        DailyLogRepository dailyLogRepository,
                        CashCollectionRepository cashCollectionRepository,
                        LeaveRequestRepository leaveRequestRepository,
                        TaskRepository taskRepository,
                        HolidayRepository holidayRepository) {
        this.recycleBinRepository = recycleBinRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.monthlyLockoutRepository = monthlyLockoutRepository;
        this.payrollRecordRepository = payrollRecordRepository;
        this.salaryAdvanceRepository = salaryAdvanceRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.cashCollectionRepository = cashCollectionRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.taskRepository = taskRepository;
        this.holidayRepository = holidayRepository;
    }

    public PurgeResult purgeAllData() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        int total = 0;

        total += deleteTable("RecycleBin", recycleBinRepository::count, recycleBinRepository::deleteAllInBatch, counts);
        total += deleteTable("Notification", notificationRepository::count, notificationRepository::deleteAllInBatch, counts);
        total += deleteTable("AuditLog", auditLogRepository::count, auditLogRepository::deleteAllInBatch, counts);
        total += deleteTable("MonthlyLockout", monthlyLockoutRepository::count, monthlyLockoutRepository::deleteAllInBatch, counts);
        total += deleteTable("PayrollRecord", payrollRecordRepository::count, payrollRecordRepository::deleteAllInBatch, counts);
        total += deleteTable("SalaryAdvance", salaryAdvanceRepository::count, salaryAdvanceRepository::deleteAllInBatch, counts);
        total += deleteTable("DailyLog", dailyLogRepository::count, dailyLogRepository::deleteAllInBatch, counts);
        total += deleteTable("CashCollection", cashCollectionRepository::count, cashCollectionRepository::deleteAllInBatch, counts);
        total += deleteTable("LeaveRequest", leaveRequestRepository::count, leaveRequestRepository::deleteAllInBatch, counts);
        total += deleteTable("Task", taskRepository::count, taskRepository::deleteAllInBatch, counts);
        total += deleteTable("Holiday", holidayRepository::count, holidayRepository::deleteAllInBatch, counts);

        return new PurgeResult(counts, total);
    }

    private int deleteTable(String name, Counter counter, BatchDeleter deleter, Map<String, Integer> counts) {
        int before = (int) counter.count();
        if (before > 0) {
            deleter.deleteAllInBatch();
        }
        counts.put(name, before);
        return before;
    }

    @FunctionalInterface
    private interface Counter {
        long count();
    }

    @FunctionalInterface
    private interface BatchDeleter {
        void deleteAllInBatch();
    }
}
