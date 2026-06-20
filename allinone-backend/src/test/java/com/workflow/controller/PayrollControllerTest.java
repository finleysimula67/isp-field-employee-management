package com.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.config.TestAuthUtil;
import com.workflow.config.TestSecurityBeans;
import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.PayrollRecord;
import com.workflow.entity.PayrollStatus;
import com.workflow.entity.Role;
import com.workflow.security.JwtAuthenticationFilter;
import com.workflow.security.OAuth2SuccessHandler;
import com.workflow.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = PayrollController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class, OAuth2SuccessHandler.class}),
    excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@Import(TestSecurityBeans.class)
class PayrollControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PayrollService payrollService;

    private static final Employee EMP_ADMIN = TestAuthUtil.createEmployee(1L, "admin@test.com", "Admin", Role.SUPER_ADMIN);
    private static final Employee EMP_MANAGER = TestAuthUtil.createEmployee(2L, "mgr@test.com", "Manager", Role.BRANCH_MANAGER);
    private static final Employee EMP_FIELD = TestAuthUtil.createEmployee(3L, "emp@test.com", "Employee", Role.FIELD_EMPLOYEE);

    @BeforeEach
    void setUp() {
        TestAuthUtil.setAuth(EMP_ADMIN);
    }

    private PayrollRecord createRecord(Long id, Long employeeId, String status) {
        Employee emp = new Employee();
        emp.setId(employeeId);
        emp.setName("Employee");
        PayrollRecord r = new PayrollRecord();
        r.setId(id);
        r.setEmployee(emp);
        r.setStatus(PayrollStatus.valueOf(status));
        r.setPeriodLabel("6/2026");
        r.setPeriodStart(LocalDate.of(2026, 6, 1));
        r.setPeriodEnd(LocalDate.of(2026, 6, 30));
        r.setDaysWorked(5);
        r.setGrossPay(BigDecimal.valueOf(4000));
        r.setNetPay(BigDecimal.valueOf(4000));
        return r;
    }

    @Test
    void getAll_shouldReturnRecords() throws Exception {
        when(payrollService.getPayrollRecords(any(), any()))
                .thenReturn(List.of(createRecord(1L, 1L, "CALCULATED")));

        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getMy_shouldReturnMyRecords() throws Exception {
        mockMvc.perform(get("/api/payroll/my"))
                .andExpect(status().isOk());
    }

    @Test
    void calculate_shouldReturnRecord() throws Exception {
        when(payrollService.calculatePayroll(any(PayrollCalculateRequest.class)))
                .thenReturn(createRecord(1L, 1L, "CALCULATED"));

        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(1L);
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");

        mockMvc.perform(post("/api/payroll/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payroll calculated"))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    @Test
    void batchCalculate_shouldReturnRecords() throws Exception {
        when(payrollService.batchCalculate(any(PayrollBatchRequest.class)))
                .thenReturn(List.of(createRecord(1L, 1L, "CALCULATED")));

        PayrollBatchRequest req = new PayrollBatchRequest();
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");

        mockMvc.perform(post("/api/payroll/batch-calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Batch payroll calculated"));
    }

    @Test
    void approve_shouldReturnApproved() throws Exception {
        when(payrollService.approvePayroll(anyLong(), anyLong()))
                .thenReturn(createRecord(1L, 1L, "APPROVED"));

        mockMvc.perform(put("/api/payroll/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payroll approved"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void pay_shouldMarkAsPaid() throws Exception {
        when(payrollService.markAsPaid(anyLong(), anyLong()))
                .thenReturn(createRecord(1L, 1L, "PAID"));

        mockMvc.perform(put("/api/payroll/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payroll marked as paid"))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void batchCalculate_shouldReturnForbiddenForManager() throws Exception {
        TestAuthUtil.setAuth(EMP_MANAGER);
        PayrollBatchRequest req = new PayrollBatchRequest();
        req.setPeriodStart("2026-06-01");
        req.setPeriodEnd("2026-06-30");

        mockMvc.perform(post("/api/payroll/batch-calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void calculate_shouldReturnForbiddenForFieldEmployee() throws Exception {
        TestAuthUtil.setAuth(EMP_FIELD);
        PayrollCalculateRequest req = new PayrollCalculateRequest();
        req.setEmployeeId(1L);

        mockMvc.perform(post("/api/payroll/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
