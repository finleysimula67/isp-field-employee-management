package com.workflow.service;

import com.workflow.dto.PurgeResult;
import com.workflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class PurgeService {

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

        total += deleteTable("RecycleBin", recycleBinRepository::count, recycleBinRepository::deleteAll, counts);
        total += deleteTable("Notification", notificationRepository::count, notificationRepository::deleteAll, counts);
        total += deleteTable("AuditLog", auditLogRepository::count, auditLogRepository::deleteAll, counts);
        total += deleteTable("MonthlyLockout", monthlyLockoutRepository::count, monthlyLockoutRepository::deleteAll, counts);
        total += deleteTable("PayrollRecord", payrollRecordRepository::count, payrollRecordRepository::deleteAll, counts);
        total += deleteTable("SalaryAdvance", salaryAdvanceRepository::count, salaryAdvanceRepository::deleteAll, counts);
        total += deleteTable("DailyLog", dailyLogRepository::count, dailyLogRepository::deleteAll, counts);
        total += deleteTable("CashCollection", cashCollectionRepository::count, cashCollectionRepository::deleteAll, counts);
        total += deleteTable("LeaveRequest", leaveRequestRepository::count, leaveRequestRepository::deleteAll, counts);
        total += deleteTable("Task", taskRepository::count, taskRepository::deleteAll, counts);
        total += deleteTable("Holiday", holidayRepository::count, holidayRepository::deleteAll, counts);

        return new PurgeResult(counts, total);
    }

    private int deleteTable(String name, Counter counter, Runnable deleter, Map<String, Integer> counts) {
        int before = (int) counter.count();
        if (before > 0) {
            deleter.run();
        }
        counts.put(name, before);
        return before;
    }

    @FunctionalInterface
    private interface Counter {
        long count();
    }
}
