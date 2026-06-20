package com.workflow.service;

import com.workflow.dto.*;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final EmailAllowListRepository emailAllowListRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(EmployeeRepository er, BranchRepository br, EmailAllowListRepository eal,
                       JwtService js, AuthenticationManager am, PasswordEncoder pe, AuditLogService als,
                       EmailService emailService) {
        this.employeeRepository = er; this.branchRepository = br; this.emailAllowListRepository = eal;
        this.jwtService = js; this.authenticationManager = am; this.passwordEncoder = pe; this.auditLogService = als;
        this.emailService = emailService;
    }

    public LoginResponse loginWithEmail(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (!Boolean.TRUE.equals(employee.getIsAccountApproved())) {
            throw new RuntimeException("Your account is pending admin approval. Please ask your manager.");
        }
        return buildResponse(jwtService.generateToken(employee), employee);
    }

    public LoginResponse loginWithGoogle(String email, String name, String googleId) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee == null) {
            employee = employeeRepository.save(
                new Employee(email, name, Role.FIELD_EMPLOYEE, null, googleId, AuthType.GOOGLE_ONLY,
                    null, true, false, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5000)));
            auditLogService.log("Employee", employee.getId(), "CREATED", null, null, email);
        }
        if (!Boolean.TRUE.equals(employee.getIsAccountApproved())) {
            throw new RuntimeException("Your account is pending admin approval. Please ask your manager.");
        }
        String token = jwtService.generateToken(employee);
        return buildResponse(token, employee);
    }

    public LoginResponse register(EmployeeRequest request) {
        if (!emailAllowListRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email not authorized: " + request.getEmail());
        if (employeeRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Employee already exists with email: " + request.getEmail());

        Employee employee = new Employee(request.getEmail(), request.getName(),
                request.getRole() != null ? request.getRole() : Role.FIELD_EMPLOYEE,
                null, null, AuthType.LOCAL_ONLY, passwordEncoder.encode(request.getPassword()),
                true, true, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(5000));
        if (request.getBranchId() != null)
            branchRepository.findById(request.getBranchId()).ifPresent(employee::setBranch);
        employee = employeeRepository.save(employee);
        auditLogService.log("Employee", employee.getId(), "CREATED", null, null, employee.getEmail());
        return buildResponse(jwtService.generateToken(employee), employee);
    }

    public boolean isEmailAllowed(String email) { return emailAllowListRepository.existsByEmail(email); }

    public void forgotPassword(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee == null) return;
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);
        employee.setResetToken(hashedToken);
        employee.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        employeeRepository.save(employee);
        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        String hashedToken = hashToken(token);
        Employee employee = employeeRepository.findByResetToken(hashedToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        if (employee.getResetTokenExpiry() == null || employee.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }
        employee.setPasswordHash(passwordEncoder.encode(newPassword));
        employee.setResetToken(null);
        employee.setResetTokenExpiry(null);
        employeeRepository.save(employee);
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    private LoginResponse buildResponse(String token, Employee employee) {
        return LoginResponse.builder()
                .token(token).email(employee.getEmail()).name(employee.getName())
                .role(employee.getRole().name()).userId(employee.getId())
                .message("Login successful").build();
    }
}
