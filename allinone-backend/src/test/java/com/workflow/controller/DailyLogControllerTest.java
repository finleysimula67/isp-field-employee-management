package com.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.config.TestAuthUtil;
import com.workflow.config.TestSecurityBeans;
import com.workflow.dto.*;
import com.workflow.entity.*;
import com.workflow.security.JwtAuthenticationFilter;
import com.workflow.security.OAuth2SuccessHandler;
import com.workflow.service.DailyLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DailyLogController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class, OAuth2SuccessHandler.class}),
    excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@Import(TestSecurityBeans.class)
class DailyLogControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private DailyLogService dailyLogService;

    private static final Employee EMP_ADMIN = TestAuthUtil.createEmployee(1L, "admin@test.com", "Admin", Role.SUPER_ADMIN);
    private static final Employee EMP_FIELD = TestAuthUtil.createEmployee(2L, "emp@test.com", "Employee", Role.FIELD_EMPLOYEE);

    @BeforeEach
    void setUp() {
        TestAuthUtil.setAuth(EMP_ADMIN);
    }

    private DailyLog createLog(Long id, Long employeeId, String status) {
        Employee emp = new Employee();
        emp.setId(employeeId);
        emp.setName("Employee " + employeeId);
        DailyLog log = new DailyLog();
        log.setId(id);
        log.setEmployee(emp);
        log.setLogDate(LocalDate.now());
        log.setStatus(LogStatus.valueOf(status));
        log.setCategory(LogCategory.NEW_FIBER_CONNECTION);
        log.setWorkDescription("Test work");
        return log;
    }

    @Test
    void getAll_shouldReturnLogs() throws Exception {
        DailyLog log = createLog(1L, 1L, "PENDING");
        when(dailyLogService.getDailyLogs(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/daily-logs").with(TestAuthUtil.withAuth(EMP_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].employeeName").value("Employee 1"));
    }

    @Test
    void getMy_shouldReturnMyLogs() throws Exception {
        DailyLog log = createLog(1L, 1L, "PENDING");
        when(dailyLogService.getMyLogs(anyLong(), any(), any()))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/daily-logs/my").with(TestAuthUtil.withAuth(EMP_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].employeeName").value("Employee 1"));
    }

    @Test
    void getById_shouldReturnLog() throws Exception {
        DailyLog log = createLog(1L, 1L, "PENDING");
        when(dailyLogService.getDailyLog(1L)).thenReturn(log);

        mockMvc.perform(get("/api/daily-logs/1").with(TestAuthUtil.withAuth(EMP_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void create_shouldReturnCreatedLog() throws Exception {
        DailyLog log = createLog(1L, 1L, "PENDING");
        when(dailyLogService.createDailyLog(any(DailyLogRequest.class), anyLong()))
                .thenReturn(log);

        DailyLogRequest request = new DailyLogRequest();
        request.setLogDate(LocalDate.now().toString());
        request.setCategory("NEW_FIBER_CONNECTION");
        request.setWorkDescription("Test");

        mockMvc.perform(post("/api/daily-logs").with(TestAuthUtil.withAuth(EMP_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Daily log submitted"));
    }

    @Test
    void review_shouldReturnReviewedLog() throws Exception {
        DailyLog log = createLog(1L, 1L, "APPROVED");
        when(dailyLogService.reviewDailyLog(anyLong(), any(DailyLogReviewRequest.class), anyLong()))
                .thenReturn(log);

        DailyLogReviewRequest reviewReq = new DailyLogReviewRequest();
        reviewReq.setStatus("APPROVED");
        reviewReq.setReviewComment("Good");

        mockMvc.perform(put("/api/daily-logs/1/review").with(TestAuthUtil.withAuth(EMP_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void batchReview_shouldReturnReviewedLogs() throws Exception {
        DailyLog log1 = createLog(1L, 1L, "APPROVED");
        DailyLog log2 = createLog(2L, 1L, "APPROVED");
        when(dailyLogService.batchReviewDailyLogs(anyList(), any(DailyLogReviewRequest.class), anyLong()))
                .thenReturn(List.of(log1, log2));

        BatchReviewRequest batchReq = new BatchReviewRequest();
        batchReq.setIds(List.of(1L, 2L));
        batchReq.setStatus("APPROVED");

        mockMvc.perform(post("/api/daily-logs/batch-review").with(TestAuthUtil.withAuth(EMP_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }
}
