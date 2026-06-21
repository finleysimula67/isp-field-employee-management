package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.security.GoogleTokenVerifier;
import com.workflow.security.JwtTokenProvider;
import com.workflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.workflow.entity.Employee;
import com.workflow.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final EmployeeRepository employeeRepository;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider,
                          GoogleTokenVerifier googleTokenVerifier,
                          EmployeeRepository employeeRepository) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleTokenVerifier = googleTokenVerifier;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithEmail(request)));
    }

    @PostMapping("/google-login")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("idToken is required"));
        }
        GoogleTokenVerifier.GoogleUser googleUser = googleTokenVerifier.verify(idToken);
        LoginResponse response = authService.loginWithGoogle(googleUser.email(), googleUser.name(), googleUser.googleId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", authService.register(request)));
    }

    @GetMapping("/client-id")
    public ResponseEntity<ApiResponse<Map<String, String>>> getClientId() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("clientId", googleTokenVerifier.getClientId())));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(authService.isEmailAllowed(email)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("If that email is registered, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful. You can now log in."));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean valid = jwtTokenProvider.validateToken(token);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("valid", valid)));
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("valid", false)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyOtp(request.getEmail(), request.getCode())));
    }

    @PostMapping("/mfa/enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> enableMfa(@AuthenticationPrincipal Employee admin) {
        Employee employee = employeeRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setMfaEnabled(true);
        employeeRepository.save(employee);
        return ResponseEntity.ok(new ApiResponse<>(true, "MFA enabled", null));
    }

    @PostMapping("/mfa/disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> disableMfa(@AuthenticationPrincipal Employee admin) {
        Employee employee = employeeRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setMfaEnabled(false);
        employee.setMfaCode(null);
        employee.setMfaCodeExpiry(null);
        employeeRepository.save(employee);
        return ResponseEntity.ok(new ApiResponse<>(true, "MFA disabled", null));
    }
}
