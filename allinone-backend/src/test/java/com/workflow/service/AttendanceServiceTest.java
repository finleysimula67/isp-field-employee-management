package com.workflow.service;

import com.workflow.dto.AttendanceResponse;
import com.workflow.dto.WageSummaryResponse;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AttendanceService.class)
@ActiveProfiles("test")
class AttendanceServiceTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DailyLogRepository dailyLogRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private HolidayRepository holidayRepository;
    @Autowired private AttendanceService attendanceService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        dailyLogRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        holidayRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("att_test@test.com");
        employee.setDailyRate(BigDecimal.valueOf(800));
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee.setIsAccountApproved(true);
        employee = employeeRepository.save(employee);
    }

    @Test
    void getMonthlyAttendance_shouldMarkPresentForApprovedLog() {
        LocalDate today = LocalDate.of(2026, 6, 1);
        dailyLogRepository.save(createLog(employee, today, LogStatus.APPROVED));

        List<AttendanceResponse> result = attendanceService.getMonthlyAttendance(6, 2026);

        assertEquals(1, result.size());
        assertEquals("PRESENT", result.get(0).getDays().get(today.toString()));
        assertEquals(1, result.get(0).getStats().getPresent());
    }

    @Test
    void getMonthlyAttendance_shouldMarkPendingLog() {
        LocalDate today = LocalDate.of(2026, 6, 1);
        dailyLogRepository.save(createLog(employee, today, LogStatus.PENDING));

        List<AttendanceResponse> result = attendanceService.getMonthlyAttendance(6, 2026);

        assertEquals("PENDING", result.get(0).getDays().get(today.toString()));
        assertEquals(1, result.get(0).getStats().getPending());
    }

    @Test
    void getMonthlyAttendance_shouldMarkApprovedLeave() {
        LocalDate today = LocalDate.of(2026, 6, 1);
        leaveRequestRepository.save(createLeave(employee, today, today));

        List<AttendanceResponse> result = attendanceService.getMonthlyAttendance(6, 2026);

        assertEquals("ON_LEAVE", result.get(0).getDays().get(today.toString()));
        assertEquals(1, result.get(0).getStats().getOnLeave());
    }

    @Test
    void getMonthlyAttendance_shouldMarkHoliday() {
        LocalDate today = LocalDate.of(2026, 6, 1);
        Holiday h = new Holiday();
        h.setDate(today);
        h.setName("Public Holiday");
        holidayRepository.save(h);

        List<AttendanceResponse> result = attendanceService.getMonthlyAttendance(6, 2026);

        assertEquals("HOLIDAY", result.get(0).getDays().get(today.toString()));
        assertEquals(1, result.get(0).getStats().getHoliday());
    }

    @Test
    void getMonthlyAttendance_shouldMarkAbsentForMissingLog() {
        List<AttendanceResponse> result = attendanceService.getMonthlyAttendance(6, 2026);

        assertEquals(30, result.get(0).getDays().size());
        assertTrue(result.get(0).getDays().values().stream().allMatch(s -> s.equals("ABSENT")));
        assertEquals(30, result.get(0).getStats().getAbsent());
    }

    @Test
    void getWageSummary_shouldCalculateCorrectly() {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1), LogStatus.APPROVED));
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 2), LogStatus.APPROVED));
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 3), LogStatus.APPROVED));

        List<WageSummaryResponse> result = attendanceService.getWageSummary(6, 2026);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getPresentDays());
        assertEquals(27, result.get(0).getAbsentDays());
        assertEquals(BigDecimal.valueOf(800), result.get(0).getDailyRate());
        assertEquals(BigDecimal.valueOf(2400), result.get(0).getTotalEarned());
    }

    @Test
    void getMyWageSummary_shouldReturnEmployeeWages() {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1), LogStatus.APPROVED));

        WageSummaryResponse result = attendanceService.getMyWageSummary(employee.getId(), 6, 2026);

        assertEquals(1, result.getPresentDays());
        assertEquals(29, result.getAbsentDays());
        assertEquals(BigDecimal.valueOf(800), result.getDailyRate());
        assertEquals(BigDecimal.valueOf(800), result.getTotalEarned());
    }

    @Test
    void getWageSummary_shouldExcludePendingLogs() {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1), LogStatus.PENDING));

        List<WageSummaryResponse> result = attendanceService.getWageSummary(6, 2026);

        assertEquals(0, result.get(0).getPresentDays());
        assertEquals(BigDecimal.ZERO, result.get(0).getTotalEarned());
    }

    private DailyLog createLog(Employee emp, LocalDate date, LogStatus status) {
        DailyLog log = new DailyLog();
        log.setEmployee(emp);
        log.setLogDate(date);
        log.setStatus(status);
        log.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        log.setWorkDescription("Test work");
        return log;
    }

    private LeaveRequest createLeave(Employee emp, LocalDate start, LocalDate end) {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setStatus(LeaveStatus.APPROVED);
        lr.setLeaveType(LeaveType.SICK);
        lr.setReason("Test leave");
        lr.setDurationDays((int) (end.toEpochDay() - start.toEpochDay()) + 1);
        return lr;
    }
}
