package com.workflow.service;

import com.workflow.dto.LockoutStatusResponse;
import com.workflow.dto.LockoutUnlockRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MonthlyLockoutService {
    private static final Logger log = LoggerFactory.getLogger(MonthlyLockoutService.class);
    private final MonthlyLockoutRepository monthlyLockoutRepository;
    private final DailyLogRepository dailyLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    public MonthlyLockoutService(MonthlyLockoutRepository mlr, DailyLogRepository dlr,
                                 EmployeeRepository er, AuditLogService als) {
        this.monthlyLockoutRepository = mlr; this.dailyLogRepository = dlr;
        this.employeeRepository = er; this.auditLogService = als;
    }

    public LockoutStatusResponse getLockoutStatus(String yearMonth) {
        LockoutStatusResponse response = new LockoutStatusResponse();
        response.setYearMonth(yearMonth);
        monthlyLockoutRepository.findByYearMonth(yearMonth).ifPresentOrElse(ml -> {
            response.setId(ml.getId());
            response.setIsLocked(!ml.getIsUnlocked());
            response.setLockedAt(ml.getLockedAt() != null ? ml.getLockedAt().toString() : null);
            response.setLockedBy(ml.getLockedBy() != null ? ml.getLockedBy().getId() : null);
            response.setLockedByName(ml.getLockedBy() != null ? ml.getLockedBy().getName() : null);
            response.setLockDay(ml.getLockDay());
            response.setIsUnlocked(ml.getIsUnlocked());
            response.setUnlockedAt(ml.getUnlockedAt() != null ? ml.getUnlockedAt().toString() : null);
            response.setUnlockedReason(ml.getUnlockReason());
        }, () -> {
            response.setIsLocked(false);
            response.setLockDay(5);
        });
        return response;
    }

    @Transactional
    public MonthlyLockout lockMonth(String yearMonth, Long lockedById) {
        MonthlyLockout lockout = monthlyLockoutRepository.findByYearMonth(yearMonth).orElseGet(() -> {
            MonthlyLockout ml = new MonthlyLockout();
            ml.setYearMonth(yearMonth);
            ml.setLockDay(LocalDate.now().getDayOfMonth());
            return ml;
        });
        lockout.setLockedAt(LocalDateTime.now());
        if (lockedById != null) {
            Employee lockedBy = employeeRepository.findById(lockedById)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            lockout.setLockedBy(lockedBy);
        }
        lockout.setIsUnlocked(false);
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        List<DailyLog> monthLogs = dailyLogRepository.findByLogDateBetween(startDate, endDate);
        for (DailyLog log : monthLogs) {
            log.setMonthLocked(true);
        }
        if (!monthLogs.isEmpty()) dailyLogRepository.saveAll(monthLogs);
        MonthlyLockout saved = monthlyLockoutRepository.save(lockout);
        auditLogService.log("MonthlyLockout", saved.getId(), "LOCKED", null, null, yearMonth);
        return saved;
    }

    @Transactional
    public MonthlyLockout unlockMonth(String yearMonth, LockoutUnlockRequest request, Long unlockedById) {
        MonthlyLockout lockout = monthlyLockoutRepository.findByYearMonth(yearMonth)
                .orElseThrow(() -> new RuntimeException("Lockout not found for " + yearMonth));
        if (lockout.getIsUnlocked())
            throw new RuntimeException("Month is already unlocked");
        Employee unlockedBy = employeeRepository.findById(unlockedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        lockout.setIsUnlocked(true);
        lockout.setUnlockedAt(LocalDateTime.now());
        lockout.setUnlockedBy(unlockedBy);
        lockout.setUnlockReason(request.getReason());
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        List<DailyLog> monthLogs = dailyLogRepository.findByLogDateBetween(startDate, endDate);
        for (DailyLog log : monthLogs) {
            log.setMonthLocked(false);
        }
        if (!monthLogs.isEmpty()) dailyLogRepository.saveAll(monthLogs);
        MonthlyLockout saved = monthlyLockoutRepository.save(lockout);
        auditLogService.log("MonthlyLockout", saved.getId(), "UNLOCKED", null, null, yearMonth);
        return saved;
    }

    public List<MonthlyLockout> getAllLockouts() {
        return monthlyLockoutRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "yearMonth"));
    }

    @Scheduled(cron = "0 0 2 6 * ?")
    @Transactional
    public void autoLockExpiredMonths() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        String yearMonth = previousMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (monthlyLockoutRepository.findByYearMonth(yearMonth).isEmpty()) {
            lockMonth(yearMonth, null);
        }
    }
}
