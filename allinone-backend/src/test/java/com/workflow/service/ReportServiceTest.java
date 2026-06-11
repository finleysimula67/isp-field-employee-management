package com.workflow.service;

import com.workflow.dto.ReportRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
class ReportServiceTest {

    @Autowired private DailyLogRepository dailyLogRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private BranchRepository branchRepository;

    private ReportService reportService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        dailyLogRepository.deleteAll();
        employeeRepository.deleteAll();

        reportService = new ReportService(dailyLogRepository, employeeRepository, branchRepository);

        employee = new Employee();
        employee.setName("Test Employee");
        employee.setEmail("report_test@test.com");
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee.setIsAccountApproved(true);
        employee = employeeRepository.save(employee);
    }

    @Test
    void generateReport_shouldBuildSummaryAndDetails() {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1)));

        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");

        Map<String, Object> report = reportService.generateReport(request);

        assertNotNull(report.get("summary"));
        assertNotNull(report.get("details"));

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) report.get("summary");
        assertEquals("2026-06-01", summary.get("startDate"));
        assertEquals("2026-06-07", summary.get("endDate"));
        assertEquals(1, summary.get("employeeCount"));
        assertEquals(1L, summary.get("totalDays"));
        assertEquals(BigDecimal.valueOf(8), summary.get("totalHours"));
    }

    @Test
    void generateReport_shouldFilterByEmployeeId() {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1)));

        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");
        request.setEmployeeId(employee.getId());

        Map<String, Object> report = reportService.generateReport(request);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) report.get("details");
        assertEquals(1, details.size());
    }

    @Test
    void generateReport_shouldReturnEmptyDetailsWhenNoLogs() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");

        Map<String, Object> report = reportService.generateReport(request);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) report.get("details");
        assertTrue(details.isEmpty());
        assertEquals(0L, ((Map<String, Object>) report.get("summary")).get("totalDays"));
    }

    @Test
    void generateReport_shouldThrowForInvalidDateRange() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-07");
        request.setEndDate("2026-06-01");

        assertThrows(RuntimeException.class, () -> reportService.generateReport(request));
    }

    @Test
    void exportReport_shouldGenerateCsv() throws Exception {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1)));

        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");
        request.setFormat("csv");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = new ServletOutputStream() {
            public void write(int b) throws IOException { baos.write(b); }
            public boolean isReady() { return true; }
            public void setWriteListener(jakarta.servlet.WriteListener wl) {}
        };
        when(response.getOutputStream()).thenReturn(sos);

        reportService.exportReport(request, response);

        String csv = baos.toString();
        assertTrue(csv.contains("ID,Employee ID,Employee Name"));
        assertTrue(csv.contains("Test Employee"));
        verify(response).setContentType("text/csv");
        verify(response).setHeader("Content-Disposition", "attachment; filename=report.csv");
    }

    @Test
    void exportReport_shouldGeneratePdf() throws Exception {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1)));

        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");
        request.setFormat("pdf");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = new ServletOutputStream() {
            public void write(int b) throws IOException { baos.write(b); }
            public boolean isReady() { return true; }
            public void setWriteListener(jakarta.servlet.WriteListener wl) {}
        };
        when(response.getOutputStream()).thenReturn(sos);

        reportService.exportReport(request, response);

        String pdfHeader = new String(baos.toByteArray(), 0, 5);
        assertEquals("%PDF-", pdfHeader);
        verify(response).setContentType("application/pdf");
    }

    @Test
    void exportReport_shouldGenerateExcel() throws Exception {
        dailyLogRepository.save(createLog(employee, LocalDate.of(2026, 6, 1)));

        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");
        request.setFormat("excel");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = new ServletOutputStream() {
            public void write(int b) throws IOException { baos.write(b); }
            public boolean isReady() { return true; }
            public void setWriteListener(jakarta.servlet.WriteListener wl) {}
        };
        when(response.getOutputStream()).thenReturn(sos);

        reportService.exportReport(request, response);

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        assertTrue(data[0] == (byte) 0x50 && data[1] == (byte) 0x4B);
        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void exportReport_shouldThrowForUnsupportedFormat() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-06-07");
        request.setFormat("xml");

        assertThrows(RuntimeException.class, () -> reportService.exportReport(request, mock(HttpServletResponse.class)));
    }

    private DailyLog createLog(Employee emp, LocalDate date) {
        DailyLog log = new DailyLog();
        log.setEmployee(emp);
        log.setLogDate(date);
        log.setHoursWorked(BigDecimal.valueOf(8));
        log.setStatus(LogStatus.APPROVED);
        log.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        log.setWorkDescription("Test work");
        return log;
    }
}
