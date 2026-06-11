package com.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.config.TestAuthUtil;
import com.workflow.config.TestSecurityBeans;
import com.workflow.dto.*;
import com.workflow.entity.Role;
import com.workflow.security.JwtAuthenticationFilter;
import com.workflow.security.OAuth2SuccessHandler;
import com.workflow.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class, OAuth2SuccessHandler.class}),
    excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@Import(TestSecurityBeans.class)
class EmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private EmployeeService employeeService;

    private static final com.workflow.entity.Employee EMP_ADMIN = TestAuthUtil.createEmployee(1L, "admin@test.com", "Admin", Role.SUPER_ADMIN);
    private static final com.workflow.entity.Employee EMP_FIELD = TestAuthUtil.createEmployee(2L, "emp@test.com", "Employee", Role.FIELD_EMPLOYEE);

    @BeforeEach
    void setUp() {
        TestAuthUtil.setAuth(EMP_ADMIN);
    }

    private EmployeeResponse createResponse(Long id, String name, String role) {
        EmployeeResponse r = new EmployeeResponse();
        r.setId(id);
        r.setEmail(name.toLowerCase().replace(" ", ".") + "@test.com");
        r.setName(name);
        r.setRole(role);
        r.setIsActive(true);
        r.setIsAccountApproved(true);
        return r;
    }

    @Test
    void getAll_shouldReturnEmployees() throws Exception {
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(createResponse(1L, "Alice", "FIELD_EMPLOYEE")));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Alice"));
    }

    @Test
    void getById_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployee(1L))
                .thenReturn(createResponse(1L, "Bob", "FIELD_EMPLOYEE"));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bob"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        EmployeeResponse resp = createResponse(3L, "Charlie", "FIELD_EMPLOYEE");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(resp);

        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("charlie@test.com");
        req.setName("Charlie");
        req.setRole(com.workflow.entity.Role.FIELD_EMPLOYEE);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee created"))
                .andExpect(jsonPath("$.data.name").value("Charlie"));
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        EmployeeResponse resp = createResponse(1L, "Updated", "FIELD_EMPLOYEE");
        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequest.class))).thenReturn(resp);

        EmployeeRequest req = new EmployeeRequest();
        req.setName("Updated");

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    void delete_shouldDeactivate() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee deactivated"));
    }

    @Test
    void approve_shouldReturnApproved() throws Exception {
        EmployeeResponse resp = createResponse(1L, "Approved", "FIELD_EMPLOYEE");
        resp.setIsAccountApproved(true);
        when(employeeService.approveEmployee(1L)).thenReturn(resp);

        mockMvc.perform(put("/api/employees/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee approved"));
    }

    @Test
    void transferOwnership_shouldTransfer() throws Exception {
        EmployeeResponse resp = createResponse(2L, "New Owner", "SUPER_ADMIN");
        resp.setIsOwner(true);
        when(employeeService.transferOwnership(anyLong(), eq(2L))).thenReturn(resp);

        mockMvc.perform(put("/api/employees/transfer-ownership/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ownership transferred"));
    }

    @Test
    void getById_shouldReturnForbiddenForFieldEmployee() throws Exception {
        TestAuthUtil.setAuth(EMP_FIELD);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldReturnForbiddenForFieldEmployee() throws Exception {
        TestAuthUtil.setAuth(EMP_FIELD);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}
