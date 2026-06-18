package com.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.config.TestSecurityBeans;
import com.workflow.dto.*;
import com.workflow.security.JwtAuthenticationFilter;
import com.workflow.security.JwtTokenProvider;
import com.workflow.security.OAuth2SuccessHandler;
import com.workflow.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class, OAuth2SuccessHandler.class}),
    excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@Import(TestSecurityBeans.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @Test
    void login_shouldReturnToken() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token");
        loginResponse.setEmail("test@test.com");
        loginResponse.setRole("FIELD_EMPLOYEE");
        loginResponse.setName("Test User");
        loginResponse.setUserId(1L);

        when(authService.loginWithEmail(any(LoginRequest.class))).thenReturn(loginResponse);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));
    }

    @Test
    void register_shouldReturnSuccess() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token");
        loginResponse.setEmail("new@test.com");

        when(authService.register(any(EmployeeRequest.class))).thenReturn(loginResponse);

        EmployeeRequest request = new EmployeeRequest();
        request.setEmail("new@test.com");
        request.setName("New User");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void forgotPassword_shouldReturnOk() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").value("If that email is registered, a reset link has been sent."));
    }

    @Test
    void resetPassword_shouldReturnOk() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("newPass123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").value("Password reset successful. You can now log in."));
    }

    @Test
    void checkEmail_shouldReturnBoolean() throws Exception {
        when(authService.isEmailAllowed("test@test.com")).thenReturn(true);

        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }
}
