package com.workflow.service;

import com.workflow.dto.EmployeeRequest;
import com.workflow.dto.LoginRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private EmailAllowListRepository emailAllowListRepository;

    private AuthService authService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        emailAllowListRepository.deleteAll();

        JwtService jwtService = new JwtService(null) {
            @Override public String generateToken(Employee emp) { return "jwt:" + emp.getId(); }
        };
        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };
        EmailService emailService = new EmailService(null) {
            @Override public void sendPasswordResetEmail(String to, String resetLink) {}
            @Override public void sendEmail(String to, String subject, String body) {}
        };
        StubPasswordEncoder passwordEncoder = new StubPasswordEncoder();
        StubAuthenticationManager authenticationManager = new StubAuthenticationManager();

        authService = new AuthService(employeeRepository, branchRepository, emailAllowListRepository,
                jwtService, authenticationManager, passwordEncoder, auditLogService, emailService);

        employee = new Employee();
        employee.setEmail("test@test.com");
        employee.setName("Test User");
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setIsAccountApproved(true);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee = employeeRepository.save(employee);
    }

    @Test
    void forgotPassword_shouldSetTokenAndSendEmail() {
        authService.forgotPassword("test@test.com");
        Employee updated = employeeRepository.findByEmail("test@test.com").get();
        assertNotNull(updated.getResetToken());
        assertNotNull(updated.getResetTokenExpiry());
    }

    @Test
    void forgotPassword_shouldNotThrowForUnknownEmail() {
        assertDoesNotThrow(() -> authService.forgotPassword("unknown@test.com"));
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void resetPassword_shouldResetWithValidToken() {
        employee.setResetToken(sha256("valid-token"));
        employee.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        employeeRepository.save(employee);

        authService.resetPassword("valid-token", "newPass123");

        Employee updated = employeeRepository.findById(employee.getId()).get();
        assertEquals("stub:newPass123", updated.getPasswordHash());
        assertNull(updated.getResetToken());
        assertNull(updated.getResetTokenExpiry());
    }

    @Test
    void resetPassword_shouldThrowForInvalidToken() {
        assertThrows(RuntimeException.class,
                () -> authService.resetPassword("invalid-token", "newPass123"));
    }

    @Test
    void resetPassword_shouldThrowForExpiredToken() {
        employee.setResetToken("expired-token");
        employee.setResetTokenExpiry(LocalDateTime.now().minusHours(1));
        employeeRepository.save(employee);

        assertThrows(RuntimeException.class,
                () -> authService.resetPassword("expired-token", "newPass123"));
    }

    @Test
    void loginWithEmail_shouldReturnTokenForApprovedUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        var response = authService.loginWithEmail(request);

        assertEquals("jwt:" + employee.getId(), response.getToken());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("FIELD_EMPLOYEE", response.getRole());
    }

    @Test
    void loginWithEmail_shouldThrowForUnapprovedUser() {
        employee.setIsAccountApproved(false);
        employeeRepository.save(employee);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        assertThrows(RuntimeException.class, () -> authService.loginWithEmail(request));
    }

    @Test
    void loginWithGoogle_shouldReturnTokenForApprovedUser() {
        Employee googleUser = new Employee();
        googleUser.setEmail("google@test.com");
        googleUser.setName("Google User");
        googleUser.setAuthType(AuthType.GOOGLE_ONLY);
        googleUser.setAuthProviderId("google-123");
        googleUser.setIsAccountApproved(true);
        googleUser.setRole(Role.FIELD_EMPLOYEE);
        googleUser.setIsActive(true);
        employeeRepository.save(googleUser);

        var response = authService.loginWithGoogle("google@test.com", "Google User", "google-123");

        assertNotNull(response.getToken());
        assertEquals("Google User", response.getName());
    }

    @Test
    void loginWithGoogle_shouldThrowForUnapprovedUser() {
        Employee googleUser = new Employee();
        googleUser.setEmail("google@test.com");
        googleUser.setName("Google User");
        googleUser.setAuthType(AuthType.GOOGLE_ONLY);
        googleUser.setAuthProviderId("google-123");
        googleUser.setIsAccountApproved(false);
        googleUser.setRole(Role.FIELD_EMPLOYEE);
        googleUser.setIsActive(true);
        employeeRepository.save(googleUser);

        assertThrows(RuntimeException.class,
                () -> authService.loginWithGoogle("google@test.com", "Google User", "google-123"));
    }

    @Test
    void loginWithGoogle_shouldCreateAndRejectNewUnapprovedUser() {
        emailAllowListRepository.save(new EmailAllowList("new@test.com"));
        assertThrows(RuntimeException.class,
                () -> authService.loginWithGoogle("new@test.com", "New User", "new-123"));
        // User should have been created in DB but not approved
        assertTrue(employeeRepository.findByEmail("new@test.com").isPresent());
        assertFalse(employeeRepository.findByEmail("new@test.com").get().getIsAccountApproved());
    }

    @Test
    void register_shouldCreateEmployee() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmail("new@test.com");
        request.setName("New User");
        request.setPassword("password");
        request.setRole(Role.FIELD_EMPLOYEE);

        EmailAllowList allow = new EmailAllowList("new@test.com");
        emailAllowListRepository.save(allow);

        var response = authService.register(request);

        assertTrue(employeeRepository.findByEmail("new@test.com").isPresent());
        assertNotNull(response.getToken());
    }

    @Test
    void register_shouldThrowForNonAllowlistedEmail() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmail("unknown@test.com");
        request.setName("Unknown");

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void register_shouldThrowForDuplicateEmail() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmail("test@test.com");
        request.setName("Duplicate");

        EmailAllowList allow = new EmailAllowList("test@test.com");
        emailAllowListRepository.save(allow);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    static class StubPasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {
        @Override public String encode(CharSequence rawPassword) { return "stub:" + rawPassword; }
        @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return true; }
    }

    static class StubAuthenticationManager implements org.springframework.security.authentication.AuthenticationManager {
        @Override public org.springframework.security.core.Authentication authenticate(org.springframework.security.core.Authentication a) { return a; }
    }
}
