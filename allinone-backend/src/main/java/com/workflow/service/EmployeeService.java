package com.workflow.service;

import com.workflow.dto.EmployeeRequest;
import com.workflow.dto.EmployeeResponse;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final BigDecimal ZERO = BigDecimal.ZERO;

    public EmployeeService(EmployeeRepository er, BranchRepository br, AuditLogService als, PasswordEncoder pe) {
        this.employeeRepository = er; this.branchRepository = br; this.auditLogService = als; this.passwordEncoder = pe;
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already exists");

        AuthType authType = request.getAuthType() != null ? request.getAuthType() : AuthType.LOCAL_ONLY;
        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = passwordEncoder.encode(request.getPassword());
        }

        Employee employee = new Employee(
                request.getEmail(), request.getName(), request.getRole(),
                null, null, authType, passwordHash,
                true, true,
                request.getWageType(),
                request.getDailyRate(), request.getHourlyWage(),
                request.getTotalLeaveDaysPerYear() != null ? request.getTotalLeaveDaysPerYear() : ZERO,
                ZERO,
                request.getMaxAdvanceLimit() != null ? request.getMaxAdvanceLimit() : BigDecimal.valueOf(5000)
        );
        if (request.getBranchId() != null)
            employee.setBranch(branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found with id " + request.getBranchId())));
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        employee = employeeRepository.save(employee);
        auditLogService.log("Employee", employee.getId(), "CREATED_ADMIN", null, null, employee.getEmail());
        return toResponse(employee);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (request.getName() != null) employee.setName(request.getName());
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        if (request.getRole() != null) employee.setRole(request.getRole());
        if (request.getBranchId() != null)
            employee.setBranch(branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found with id " + request.getBranchId())));
        if (request.getWageType() != null) employee.setWageType(request.getWageType());
        if (request.getDailyRate() != null) employee.setDailyRate(request.getDailyRate());
        if (request.getHourlyWage() != null) employee.setHourlyWage(request.getHourlyWage());
        if (request.getTotalLeaveDaysPerYear() != null) employee.setTotalLeaveDaysPerYear(request.getTotalLeaveDaysPerYear());
        if (request.getRemainingLeaveDays() != null) employee.setRemainingLeaveDays(request.getRemainingLeaveDays());
        if (request.getCarryOverLeave() != null) employee.setCarryOverLeave(request.getCarryOverLeave());
        if (request.getMaxAdvanceLimit() != null) employee.setMaxAdvanceLimit(request.getMaxAdvanceLimit());
        return toResponse(employeeRepository.save(employee));
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public EmployeeResponse getEmployee(Long id) {
        return toResponse(employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found")));
    }

    public EmployeeResponse approveEmployee(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        e.setIsAccountApproved(true);
        e = employeeRepository.save(e);
        auditLogService.log("Employee", id, "APPROVED", null, null, e.getEmail());
        return toResponse(e);
    }

    public EmployeeResponse transferOwnership(Long currentOwnerId, Long targetId) {
        Employee current = employeeRepository.findById(currentOwnerId)
                .orElseThrow(() -> new RuntimeException("Current owner not found"));
        if (!Boolean.TRUE.equals(current.getIsOwner()))
            throw new RuntimeException("Only the current owner can transfer ownership");
        Employee target = employeeRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target employee not found"));
        if (!Boolean.TRUE.equals(target.getIsAccountApproved()) || !Boolean.TRUE.equals(target.getIsActive()))
            throw new RuntimeException("Target employee must be approved and active");
        if (target.getRole() != Role.SUPER_ADMIN) {
            target.setRole(Role.SUPER_ADMIN);
        }
        current.setIsOwner(false);
        target.setIsOwner(true);
        employeeRepository.save(current);
        employeeRepository.save(target);
        auditLogService.log("Employee", targetId, "OWNERSHIP_TRANSFERRED",
                currentOwnerId + " -> " + targetId, null, target.getEmail());
        return toResponse(target);
    }

    public void deleteEmployee(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        e.setIsActive(false);
        employeeRepository.save(e);
        auditLogService.log("Employee", id, "DEACTIVATED", null, null, e.getEmail());
    }

    private EmployeeResponse toResponse(Employee e) {
        EmployeeResponse r = new EmployeeResponse();
        r.setId(e.getId()); r.setEmail(e.getEmail()); r.setName(e.getName());
        r.setRole(e.getRole().name());
        r.setPhone(e.getPhone());
        r.setBranchName(e.getBranch() != null ? e.getBranch().getName() : null);
        r.setBranchId(e.getBranch() != null ? e.getBranch().getId() : null);
        r.setIsActive(e.getIsActive()); r.setIsAccountApproved(e.getIsAccountApproved());
        r.setIsOwner(e.getIsOwner());
        r.setWageType(e.getWageType() != null ? e.getWageType().name() : null);
        r.setDailyRate(e.getDailyRate()); r.setHourlyWage(e.getHourlyWage());
        r.setTotalLeaveDaysPerYear(e.getTotalLeaveDaysPerYear());
        r.setRemainingLeaveDays(e.getRemainingLeaveDays());
        r.setMaxAdvanceLimit(e.getMaxAdvanceLimit());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
