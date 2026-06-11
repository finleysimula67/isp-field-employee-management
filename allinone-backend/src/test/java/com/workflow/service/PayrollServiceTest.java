package com.workflow.service;

import com.workflow.dto.PayrollBatchRequest;
import com.workflow.dto.PayrollCalculateRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PayrollServiceTest {

    @Autowired private PayrollRecordRepository payrollRecordRepository;
    @Autowired private DailyLogRepository dailyLogRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private SalaryAdvanceRepository salaryAdvanceRepository;
    @Autowired private MonthlyLockoutRepository monthlyLockoutRepository;

    private PayrollService payrollService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        dailyLogRepository.deleteAll();
        payrollRecordRepository.deleteAll();
        salaryAdvanceRepository.deleteAll();

        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };

        payrollService = new PayrollService(payrollRecordRepository, dailyLogRepository, employeeRepository,
                salaryAdvanceRepository, monthlyLockoutRepository, auditLogService);

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
    }

    @Test
    void calculatePayroll_shouldCreateCalculatedRecord() {
        createApprovedLog(employee, LocalDate.of(2026, 6, 1));
        createApprovedLog(employee, LocalDate.of(2026, 6, 2));

        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(employee.getId());
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");

        PayrollRecord result = payrollService.calculatePayroll(req);

        assertEquals(PayrollStatus.CALCULATED, result.getStatus());
        assertEquals(2, result.getDaysWorked());
        assertEquals(0, BigDecimal.valueOf(1600).compareTo(result.getGrossPay()));
    }

    @Test
    void calculatePayroll_shouldHandleHourlyWage() {
        employee.setWageType(WageType.HOURLY);
        employee.setHourlyWage(BigDecimal.valueOf(100));
        employee.setDailyRate(null);
        employeeRepository.save(employee);

        DailyLog log = new DailyLog();
        log.setEmployee(employee);
        log.setLogDate(LocalDate.of(2026, 6, 1));
        log.setHoursWorked(BigDecimal.valueOf(8));
        log.setStatus(LogStatus.APPROVED);
        log.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        log.setWorkDescription("Test");
        dailyLogRepository.save(log);

        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(employee.getId());
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");

        PayrollRecord result = payrollService.calculatePayroll(req);

        assertEquals(1, result.getDaysWorked());
        assertEquals(0, BigDecimal.valueOf(800).compareTo(result.getGrossPay()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(result.getWageRateAtTime()));
    }

    @Test
    void calculatePayroll_shouldOnlyCountApprovedLogs() {
        DailyLog pendingLog = new DailyLog();
        pendingLog.setEmployee(employee);
        pendingLog.setLogDate(LocalDate.of(2026, 6, 1));
        pendingLog.setStatus(LogStatus.PENDING);
        pendingLog.setHoursWorked(BigDecimal.valueOf(8));
        pendingLog.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        pendingLog.setWorkDescription("Test");
        dailyLogRepository.save(pendingLog);

        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(employee.getId());
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");

        PayrollRecord result = payrollService.calculatePayroll(req);

        assertEquals(0, result.getDaysWorked());
        assertEquals(BigDecimal.ZERO, result.getGrossPay());
    }

    @Test
    void getPayrollRecords_shouldFilterByEmployee() {
        createApprovedLog(employee, LocalDate.of(2026, 6, 1));
        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(employee.getId());
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");
        payrollService.calculatePayroll(req);

        List<PayrollRecord> records = payrollService.getPayrollRecords(employee.getId(), null);
        assertEquals(1, records.size());
    }

    @Test
    void approvePayroll_shouldSetApproved() {
        PayrollRecord record = createCalculatedPayroll();
        PayrollRecord approved = payrollService.approvePayroll(record.getId(), 1L);
        assertEquals(PayrollStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approvePayroll_shouldFailForNonCalculated() {
        PayrollRecord record = createCalculatedPayroll();
        payrollService.approvePayroll(record.getId(), 1L);
        assertThrows(RuntimeException.class,
                () -> payrollService.approvePayroll(record.getId(), 1L));
    }

    @Test
    void markAsPaid_shouldSetPaidAndSettleAdvances() {
        Employee admin = new Employee();
        admin.setName("Admin");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setAuthType(AuthType.LOCAL_ONLY);
        admin.setIsActive(true);
        admin.setIsAccountApproved(true);
        admin = employeeRepository.save(admin);

        SalaryAdvance advance = new SalaryAdvance();
        advance.setEmployee(employee);
        advance.setAmount(BigDecimal.valueOf(500));
        advance.setStatus(AdvanceStatus.DISBURSED);
        advance.setIsSettled(false);
        advance.setRequestDate(LocalDate.now());
        salaryAdvanceRepository.save(advance);

        PayrollRecord record = createCalculatedPayroll();
        payrollService.approvePayroll(record.getId(), admin.getId());
        PayrollRecord paid = payrollService.markAsPaid(record.getId(), admin.getId());

        assertEquals(PayrollStatus.PAID, paid.getStatus());
        assertNotNull(paid.getPaidAt());

        SalaryAdvance settled = salaryAdvanceRepository.findById(advance.getId()).get();
        assertTrue(settled.getIsSettled());
        assertEquals(paid.getId(), settled.getSettledInPayrollId());
    }

    @Test
    void markAsPaid_shouldFailForNonApproved() {
        PayrollRecord record = createCalculatedPayroll();
        assertThrows(RuntimeException.class,
                () -> payrollService.markAsPaid(record.getId(), 1L));
    }

    @Test
    void batchCalculate_shouldCalculateForAllActive() {
        createApprovedLog(employee, LocalDate.of(2026, 6, 1));

        Employee employee2 = new Employee();
        employee2.setName("Employee 2");
        employee2.setEmail("emp2@test.com");
        employee2.setRole(Role.FIELD_EMPLOYEE);
        employee2.setAuthType(AuthType.LOCAL_ONLY);
        employee2.setIsActive(true);
        employee2.setIsAccountApproved(true);
        employee2.setDailyRate(BigDecimal.valueOf(800));
        employee2.setWageType(WageType.DAILY);
        employee2 = employeeRepository.save(employee2);

        Employee inactive = new Employee();
        inactive.setName("Inactive");
        inactive.setEmail("inactive@test.com");
        inactive.setRole(Role.FIELD_EMPLOYEE);
        inactive.setAuthType(AuthType.LOCAL_ONLY);
        inactive.setIsActive(false);
        inactive.setIsAccountApproved(true);
        inactive.setDailyRate(BigDecimal.valueOf(800));
        inactive.setWageType(WageType.DAILY);
        employeeRepository.save(inactive);

        PayrollBatchRequest batchReq = new PayrollBatchRequest();
        batchReq.setPeriodStart("2026-06-01");
        batchReq.setPeriodEnd("2026-06-30");

        List<PayrollRecord> results = payrollService.batchCalculate(batchReq);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getStatus() == PayrollStatus.CALCULATED));
    }

    private void createApprovedLog(Employee emp, LocalDate date) {
        DailyLog log = new DailyLog();
        log.setEmployee(emp);
        log.setLogDate(date);
        log.setStartTime(LocalTime.of(9, 0));
        log.setEndTime(LocalTime.of(17, 0));
        log.setHoursWorked(BigDecimal.valueOf(8));
        log.setStatus(LogStatus.APPROVED);
        log.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        log.setWorkDescription("Test");
        dailyLogRepository.save(log);
    }

    private PayrollRecord createCalculatedPayroll() {
        createApprovedLog(employee, LocalDate.of(2026, 6, 1));
        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(employee.getId());
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");
        return payrollService.calculatePayroll(req);
    }
}
