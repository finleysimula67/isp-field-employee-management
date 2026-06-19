package com.workflow.service;

import com.workflow.dto.DailyLogRequest;
import com.workflow.dto.DailyLogReviewRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DailyLogServiceTest {

    @Autowired private DailyLogRepository dailyLogRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private PayrollRecordRepository payrollRecordRepository;

    private DailyLogService dailyLogService;
    private Employee employee;
    private Employee admin;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        dailyLogRepository.deleteAll();
        notificationRepository.deleteAll();
        payrollRecordRepository.deleteAll();

        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };
        EmailService emailService = new EmailService(null) {
            @Override public void sendEmail(String to, String subject, String body) {}
            @Override public void sendPasswordResetEmail(String to, String resetLink) {}
        };
        NotificationService notificationService = new NotificationService(notificationRepository, employeeRepository, null) {
            @Override public void broadcastNotificationToRecipient(Notification notification) {}
            @Override public void broadcastNotification(Long recipientId, Notification notification) {}
            @Override public void broadcastUnreadCount(Long recipientId) {}
        };
        RecycleBinService recycleBinService = new RecycleBinService(null, null) {
            @Override public void softDelete(Object entity, Long entityId, String entityType, Employee actor, Long originalOwnerId, java.time.LocalDateTime originalCreatedAt) {}
            @Override public void bulkDeleteLogged(String entityType, int count, Employee actor) {}
        };

        dailyLogService = new DailyLogService(dailyLogRepository, employeeRepository, branchRepository,
                notificationRepository, auditLogService, payrollRecordRepository, emailService, notificationService, recycleBinService);

        employee = new Employee();
        employee.setName("Field Employee");
        employee.setEmail("emp@test.com");
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee.setIsAccountApproved(true);
        employee.setDailyRate(BigDecimal.valueOf(800));
        employee.setWageType(WageType.DAILY);
        employee = employeeRepository.save(employee);

        admin = new Employee();
        admin.setName("Super Admin");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setAuthType(AuthType.LOCAL_ONLY);
        admin.setIsActive(true);
        admin.setIsAccountApproved(true);
        admin = employeeRepository.save(admin);
    }

    @Test
    void createDailyLog_shouldSetPendingStatus() {
        DailyLogRequest request = createRequest(employee.getId());
        DailyLog result = dailyLogService.createDailyLog(request, employee.getId());
        assertEquals(LogStatus.PENDING, result.getStatus());
    }

    @Test
    void createDailyLog_shouldParseTimes() {
        DailyLogRequest request = createRequest(employee.getId());
        request.setStartTime("09:00");
        request.setEndTime("17:00");
        request.setHoursWorked(8.0);
        DailyLog result = dailyLogService.createDailyLog(request, employee.getId());
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertEquals(LocalTime.of(17, 0), result.getEndTime());
        assertEquals(0, BigDecimal.valueOf(8).compareTo(result.getHoursWorked()));
    }

    @Test
    void createDailyLog_shouldCreateNotificationForAdmins() {
        DailyLogRequest request = createRequest(employee.getId());
        dailyLogService.createDailyLog(request, employee.getId());
        List<Notification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.stream().allMatch(n -> n.getType().equals("DAILY_LOG_SUBMITTED")));
    }

    @Test
    void getDailyLog_shouldReturnLog() {
        DailyLogRequest request = createRequest(employee.getId());
        DailyLog created = dailyLogService.createDailyLog(request, employee.getId());
        DailyLog found = dailyLogService.getDailyLog(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getDailyLog_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> dailyLogService.getDailyLog(999L));
    }

    @Test
    void getDailyLogs_shouldFilterByEmployeeId() {
        dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        List<DailyLog> logs = dailyLogService.getDailyLogs(employee.getId(), null, null, 0, 100);
        assertEquals(1, logs.size());
    }

    @Test
    void getDailyLogs_shouldFilterByStatus() {
        dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        List<DailyLog> pending = dailyLogService.getDailyLogs(null, "PENDING", null, 0, 100);
        assertEquals(1, pending.size());
        List<DailyLog> approved = dailyLogService.getDailyLogs(null, "APPROVED", null, 0, 100);
        assertTrue(approved.isEmpty());
    }

    @Test
    void reviewDailyLog_shouldApproveAndAutoCreditPayroll() {
        DailyLog log = dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus("APPROVED");
        reviewReq.setReviewComment("Good work");
        DailyLog reviewed = dailyLogService.reviewDailyLog(log.getId(), reviewReq, admin.getId());
        assertEquals(LogStatus.APPROVED, reviewed.getStatus());
        assertEquals("Good work", reviewed.getReviewComment());
        List<PayrollRecord> payrolls = payrollRecordRepository.findByEmployeeIdOrderByPeriodStartDesc(employee.getId());
        assertFalse(payrolls.isEmpty());
        PayrollRecord payroll = payrolls.get(0);
        assertEquals(PayrollStatus.CALCULATED, payroll.getStatus());
        assertEquals(1, payroll.getDaysWorked());
        assertEquals(0, BigDecimal.valueOf(800).compareTo(payroll.getGrossPay()));
    }

    @Test
    void reviewDailyLog_shouldRejectLog() {
        DailyLog log = dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus("REJECTED");
        reviewReq.setReviewComment("Incomplete");
        DailyLog reviewed = dailyLogService.reviewDailyLog(log.getId(), reviewReq, admin.getId());
        assertEquals(LogStatus.REJECTED, reviewed.getStatus());
        List<PayrollRecord> payrolls = payrollRecordRepository.findByEmployeeIdOrderByPeriodStartDesc(employee.getId());
        assertTrue(payrolls.isEmpty());
    }

    @Test
    void reviewDailyLog_shouldFailForNonPending() {
        DailyLog log = dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus("APPROVED");
        dailyLogService.reviewDailyLog(log.getId(), reviewReq, admin.getId());
        assertThrows(RuntimeException.class,
                () -> dailyLogService.reviewDailyLog(log.getId(), reviewReq, admin.getId()));
    }

    @Test
    void batchReviewDailyLogs_shouldReviewMultiple() {
        DailyLog log1 = dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        DailyLog log2 = dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus("APPROVED");
        reviewReq.setReviewComment("Batch approved");
        List<DailyLog> reviewed = dailyLogService.batchReviewDailyLogs(
                List.of(log1.getId(), log2.getId()), reviewReq, admin.getId());
        assertEquals(2, reviewed.size());
        assertTrue(reviewed.stream().allMatch(l -> l.getStatus() == LogStatus.APPROVED));
    }

    @Test
    void getMyLogs_shouldReturnEmployeeLogs() {
        dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        List<DailyLog> logs = dailyLogService.getMyLogs(employee.getId(), null, null);
        assertEquals(1, logs.size());
    }

    @Test
    void getMyLogs_shouldFilterByCategory() {
        DailyLogRequest req = createRequest(employee.getId());
        req.setCategory("SERVICE_MAINTENANCE");
        dailyLogService.createDailyLog(req, employee.getId());
        List<DailyLog> filtered = dailyLogService.getMyLogs(employee.getId(), "SERVICE_MAINTENANCE", null);
        assertEquals(1, filtered.size());
        List<DailyLog> noMatch = dailyLogService.getMyLogs(employee.getId(), "NEW_FIBER_CONNECTION", null);
        assertTrue(noMatch.isEmpty());
    }

    @Test
    void getEarningsSummary_shouldCalculateCorrectly() {
        dailyLogService.createDailyLog(createRequest(employee.getId()), employee.getId());
        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus("APPROVED");
        dailyLogService.reviewDailyLog(
                dailyLogService.getMyLogs(employee.getId(), null, null).get(0).getId(),
                reviewReq, admin.getId());
        Map<String, Object> earnings = dailyLogService.getEarningsSummary(employee.getId());
        assertEquals(1L, earnings.get("approvedDays"));
        assertEquals(0, BigDecimal.valueOf(800).compareTo((BigDecimal) earnings.get("dailyRate")));
        assertEquals(0, BigDecimal.valueOf(800).compareTo((BigDecimal) earnings.get("totalEarned")));
    }

    private DailyLogRequest createRequest(Long employeeId) {
        DailyLogRequest req = new DailyLogRequest();
        req.setEmployeeId(employeeId);
        req.setLogDate(LocalDate.now().toString());
        req.setCategory("NEW_FIBER_CONNECTION");
        req.setWorkDescription("Test work description");
        return req;
    }
}
