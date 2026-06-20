package com.workflow.service;

import com.workflow.dto.AdvanceReviewRequest;
import com.workflow.dto.ManualAdvanceRequest;
import com.workflow.dto.SalaryAdvanceRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SalaryAdvanceServiceTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DailyLogRepository dailyLogRepository;
    @Autowired private SalaryAdvanceRepository salaryAdvanceRepository;
    @Autowired private PayrollRecordRepository payrollRecordRepository;
    @Autowired private NotificationRepository notificationRepository;

    private SalaryAdvanceService salaryAdvanceService;
    private Employee employee;
    private Employee admin;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        salaryAdvanceRepository.deleteAll();
        dailyLogRepository.deleteAll();
        notificationRepository.deleteAll();

        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };
        StubEmailService emailService = new StubEmailService();
        StubNotificationService notificationService = new StubNotificationService(notificationRepository, employeeRepository);
        RecycleBinService recycleBinService = new RecycleBinService(null, null) {
            @Override public void softDelete(Object entity, Long entityId, String entityType, Employee actor, Long originalOwnerId, java.time.LocalDateTime originalCreatedAt) {}
            @Override public void bulkDeleteLogged(String entityType, int count, Employee actor) {}
        };

        salaryAdvanceService = new SalaryAdvanceService(salaryAdvanceRepository, employeeRepository,
                dailyLogRepository, payrollRecordRepository, notificationRepository,
                auditLogService, emailService, notificationService, recycleBinService);

        employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("emp@test.com");
        employee.setDailyRate(BigDecimal.valueOf(800));
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee.setIsAccountApproved(true);
        employee.setMaxAdvanceLimit(BigDecimal.valueOf(5000));
        employee = employeeRepository.save(employee);

        admin = new Employee();
        admin.setName("Admin");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setAuthType(AuthType.LOCAL_ONLY);
        admin.setIsActive(true);
        admin.setIsAccountApproved(true);
        admin = employeeRepository.save(admin);
    }

    @Test
    void getEmployeeBalance_shouldReturnZeroWhenNoLogsOrAdvances() {
        Map<String, BigDecimal> balance = salaryAdvanceService.getEmployeeBalance(employee.getId());

        assertEquals(BigDecimal.ZERO, balance.get("totalEarned"));
        assertEquals(BigDecimal.ZERO, balance.get("totalAdvanced"));
        assertEquals(BigDecimal.valueOf(5000), balance.get("availableForAdvance"));
    }

    @Test
    void getEmployeeBalance_shouldCalculateEarningsFromApprovedDays() {
        saveLogsWithStatus(employee, 5, LogStatus.APPROVED);

        Map<String, BigDecimal> balance = salaryAdvanceService.getEmployeeBalance(employee.getId());

        assertEquals(BigDecimal.valueOf(4000), balance.get("totalEarned"));
        assertEquals(BigDecimal.valueOf(5000), balance.get("availableForAdvance"));
    }

    @Test
    void getEmployeeBalance_shouldSubtractAdvancesFromEarnings() {
        saveLogsWithStatus(employee, 5, LogStatus.APPROVED);
        salaryAdvanceRepository.save(createAdvance(employee, BigDecimal.valueOf(1000), AdvanceStatus.DISBURSED));

        Map<String, BigDecimal> balance = salaryAdvanceService.getEmployeeBalance(employee.getId());

        assertEquals(BigDecimal.valueOf(4000), balance.get("totalEarned"));
        assertEquals(BigDecimal.valueOf(1000), balance.get("totalAdvanced"));
        assertEquals(BigDecimal.valueOf(4000), balance.get("availableForAdvance"));
    }

    @Test
    void getEmployeeBalance_shouldNotCountRejectedAdvances() {
        saveLogsWithStatus(employee, 5, LogStatus.APPROVED);
        salaryAdvanceRepository.save(createAdvance(employee, BigDecimal.valueOf(1000), AdvanceStatus.REJECTED));

        Map<String, BigDecimal> balance = salaryAdvanceService.getEmployeeBalance(employee.getId());

        assertEquals(BigDecimal.ZERO, balance.get("totalAdvanced"));
        assertEquals(BigDecimal.valueOf(5000), balance.get("availableForAdvance"));
    }

    @Test
    void getEmployeeBalance_shouldNotCountSettledAdvances() {
        saveLogsWithStatus(employee, 10, LogStatus.APPROVED);
        SalaryAdvance settledAdvance = createAdvance(employee, BigDecimal.valueOf(3000), AdvanceStatus.SETTLED);
        settledAdvance.setIsSettled(true);
        salaryAdvanceRepository.save(settledAdvance);

        Map<String, BigDecimal> balance = salaryAdvanceService.getEmployeeBalance(employee.getId());

        assertEquals(BigDecimal.valueOf(8000), balance.get("totalEarned"));
        assertEquals(BigDecimal.ZERO, balance.get("totalAdvanced"));
        assertEquals(BigDecimal.valueOf(5000), balance.get("availableForAdvance"));
    }

    @Test
    void getEmployeeBalance_shouldNotGoNegative() {
        salaryAdvanceRepository.save(createAdvance(employee, BigDecimal.valueOf(1000), AdvanceStatus.DISBURSED));

        Map<String, BigDecimal> balance = salaryAdvanceService.getEmployeeBalance(employee.getId());

        assertEquals(BigDecimal.valueOf(4000), balance.get("availableForAdvance"));
    }

    @Test
    void requestAdvance_shouldSucceedWhenWithinLimit() {
        saveLogsWithStatus(employee, 5, LogStatus.APPROVED);

        SalaryAdvanceRequest request = new SalaryAdvanceRequest();
        request.setAmount(BigDecimal.valueOf(500.0));
        request.setReason("Test reason");

        SalaryAdvance result = salaryAdvanceService.requestAdvance(request, employee.getId());

        assertEquals(AdvanceStatus.PENDING, result.getStatus());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(result.getAmount()));
    }

    @Test
    void requestAdvance_shouldFailWhenExceedsLimit() {
        saveLogsWithStatus(employee, 1, LogStatus.APPROVED);

        SalaryAdvanceRequest request = new SalaryAdvanceRequest();
        request.setAmount(BigDecimal.valueOf(6000.0));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> salaryAdvanceService.requestAdvance(request, employee.getId()));
        assertTrue(ex.getMessage().contains("exceeds available limit"));
    }

    @Test
    void createManualAdvance_shouldSucceedWithinLimit() {
        saveLogsWithStatus(employee, 10, LogStatus.APPROVED);

        ManualAdvanceRequest request = new ManualAdvanceRequest();
        request.setEmployeeId(employee.getId());
        request.setAmount(BigDecimal.valueOf(2000.0));
        request.setReason("Manual advance");

        SalaryAdvance result = salaryAdvanceService.createManualAdvance(request, admin.getId());

        assertEquals(AdvanceStatus.DISBURSED, result.getStatus());
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(result.getAmount()));
    }

    @Test
    void createManualAdvance_shouldFailWhenExceedsLimit() {
        saveLogsWithStatus(employee, 1, LogStatus.APPROVED);

        ManualAdvanceRequest request = new ManualAdvanceRequest();
        request.setEmployeeId(employee.getId());
        request.setAmount(BigDecimal.valueOf(10000.0));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> salaryAdvanceService.createManualAdvance(request, admin.getId()));
        assertTrue(ex.getMessage().contains("exceeds available limit"));
    }

    @Test
    void reviewAdvance_shouldApprove() {
        SalaryAdvance advance = salaryAdvanceRepository.save(createAdvance(employee, BigDecimal.valueOf(500), AdvanceStatus.PENDING));

        AdvanceReviewRequest reviewReq = new AdvanceReviewRequest();
        reviewReq.setStatus("APPROVED");
        reviewReq.setNotes("Approved");

        SalaryAdvance result = salaryAdvanceService.reviewAdvance(advance.getId(), reviewReq, admin.getId());

        assertEquals(AdvanceStatus.APPROVED, result.getStatus());
        assertNotNull(result.getApprovedAt());
        assertNotNull(result.getDisbursedAt());
    }

    @Test
    void reviewAdvance_shouldReject() {
        SalaryAdvance advance = salaryAdvanceRepository.save(createAdvance(employee, BigDecimal.valueOf(500), AdvanceStatus.PENDING));

        AdvanceReviewRequest reviewReq = new AdvanceReviewRequest();
        reviewReq.setStatus("REJECTED");
        reviewReq.setNotes("Not approved");

        SalaryAdvance result = salaryAdvanceService.reviewAdvance(advance.getId(), reviewReq, admin.getId());

        assertEquals(AdvanceStatus.REJECTED, result.getStatus());
        assertNull(result.getDisbursedAt());
    }

    @Test
    void reviewAdvance_shouldFailForNonPending() {
        SalaryAdvance advance = salaryAdvanceRepository.save(createAdvance(employee, BigDecimal.valueOf(500), AdvanceStatus.APPROVED));

        AdvanceReviewRequest reviewReq = new AdvanceReviewRequest();
        reviewReq.setStatus("APPROVED");

        assertThrows(RuntimeException.class,
                () -> salaryAdvanceService.reviewAdvance(advance.getId(), reviewReq, admin.getId()));
    }

    private void saveLogsWithStatus(Employee emp, int count, LogStatus status) {
        for (int i = 1; i <= count; i++) {
            DailyLog log = new DailyLog();
            log.setEmployee(emp);
            log.setLogDate(java.time.LocalDate.of(2026, 6, i));
            log.setStatus(status);
            log.setCategory(LogCategory.NEW_FIBER_CONNECTION);
            log.setWorkDescription("Test");
            dailyLogRepository.save(log);
        }
    }

    private SalaryAdvance createAdvance(Employee emp, BigDecimal amount, AdvanceStatus status) {
        SalaryAdvance a = new SalaryAdvance();
        a.setEmployee(emp);
        a.setAmount(amount);
        a.setRequestDate(java.time.LocalDate.now());
        a.setStatus(status);
        a.setIsSettled(false);
        if (status == AdvanceStatus.SETTLED) a.setIsSettled(true);
        return a;
    }

    static class StubEmailService extends EmailService {
        StubEmailService() { super(null); }
        @Override public void sendEmail(String to, String subject, String body) {}
        @Override public void sendPasswordResetEmail(String to, String resetLink) {}
    }

    static class StubNotificationService extends NotificationService {
        StubNotificationService(NotificationRepository nr, EmployeeRepository er) {
            super(nr, er, null);
        }
        @Override public void broadcastNotification(Long recipientId, Notification notification) {}
        @Override public void broadcastUnreadCount(Long recipientId) {}
        @Override public void broadcastNotificationToRecipient(Notification notification) {}
    }
}
