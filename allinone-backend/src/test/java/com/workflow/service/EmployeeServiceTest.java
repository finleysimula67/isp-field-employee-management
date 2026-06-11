package com.workflow.service;

import com.workflow.dto.EmployeeRequest;
import com.workflow.dto.EmployeeResponse;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeServiceTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private BranchRepository branchRepository;

    private EmployeeService employeeService;
    private Branch branch;
    private Employee owner;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        branchRepository.deleteAll();

        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };
        PasswordEncoder passwordEncoder = new PasswordEncoder() {
            @Override public String encode(CharSequence rawPassword) { return "encoded:" + rawPassword; }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return true; }
        };

        employeeService = new EmployeeService(employeeRepository, branchRepository, auditLogService, passwordEncoder);

        branch = branchRepository.save(new Branch("Head Office", "HQ", "Kathmandu"));

        owner = new Employee();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner.setRole(Role.SUPER_ADMIN);
        owner.setAuthType(AuthType.LOCAL_ONLY);
        owner.setIsActive(true);
        owner.setIsAccountApproved(true);
        owner.setIsOwner(true);
        owner.setTotalLeaveDaysPerYear(BigDecimal.valueOf(20));
        owner.setRemainingLeaveDays(BigDecimal.valueOf(20));
        employeeRepository.save(owner);
    }

    @Test
    void createEmployee_shouldCreateWithDefaults() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("new@test.com");
        req.setName("New Employee");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setPassword("pass123");
        req.setBranchId(branch.getId());
        req.setWageType(WageType.DAILY);
        req.setTotalLeaveDaysPerYear(BigDecimal.valueOf(20));

        EmployeeResponse result = employeeService.createEmployee(req);

        assertNotNull(result.getId());
        assertEquals("new@test.com", result.getEmail());
        assertEquals(Role.FIELD_EMPLOYEE.name(), result.getRole());
        assertEquals("Head Office", result.getBranchName());
        assertEquals("DAILY", result.getWageType());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(result.getRemainingLeaveDays()));
    }

    @Test
    void createEmployee_shouldFailForDuplicateEmail() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("owner@test.com");
        req.setName("Duplicate");
        req.setRole(Role.FIELD_EMPLOYEE);

        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(req));
    }

    @Test
    void createEmployee_shouldRespectWageType() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("hourly@test.com");
        req.setName("Hourly Worker");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setWageType(WageType.HOURLY);
        req.setHourlyWage(BigDecimal.valueOf(150));
        req.setPassword("pass123");

        EmployeeResponse result = employeeService.createEmployee(req);

        assertEquals("HOURLY", result.getWageType());
        assertEquals(0, BigDecimal.valueOf(150).compareTo(result.getHourlyWage()));
    }

    @Test
    void updateEmployee_shouldUpdateName() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("new@test.com");
        req.setName("New Employee");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setPassword("pass123");
        EmployeeResponse created = employeeService.createEmployee(req);

        EmployeeRequest updateReq = new EmployeeRequest();
        updateReq.setName("Updated Name");
        EmployeeResponse updated = employeeService.updateEmployee(created.getId(), updateReq);

        assertEquals("Updated Name", updated.getName());
        assertEquals(created.getEmail(), updated.getEmail());
    }

    @Test
    void getEmployee_shouldReturn() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("get@test.com");
        req.setName("Get Test");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setPassword("pass123");
        EmployeeResponse created = employeeService.createEmployee(req);

        EmployeeResponse found = employeeService.getEmployee(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void getEmployee_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> employeeService.getEmployee(999L));
    }

    @Test
    void getAllEmployees_shouldReturnAll() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("another@test.com");
        req.setName("Another");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setPassword("pass123");
        employeeService.createEmployee(req);

        List<EmployeeResponse> all = employeeService.getAllEmployees();
        assertEquals(2, all.size());
    }

    @Test
    void approveEmployee_shouldSetApproved() {
        Employee unapproved = new Employee();
        unapproved.setName("Unapproved");
        unapproved.setEmail("unapproved@test.com");
        unapproved.setRole(Role.FIELD_EMPLOYEE);
        unapproved.setAuthType(AuthType.LOCAL_ONLY);
        unapproved.setIsActive(true);
        unapproved.setIsAccountApproved(false);
        unapproved.setTotalLeaveDaysPerYear(BigDecimal.valueOf(20));
        unapproved.setRemainingLeaveDays(BigDecimal.valueOf(20));
        unapproved = employeeRepository.save(unapproved);

        assertFalse(unapproved.getIsAccountApproved());

        EmployeeResponse approved = employeeService.approveEmployee(unapproved.getId());
        assertTrue(approved.getIsAccountApproved());
    }

    @Test
    void transferOwnership_shouldTransfer() {
        Employee target = new Employee();
        target.setName("Target");
        target.setEmail("target@test.com");
        target.setRole(Role.FIELD_EMPLOYEE);
        target.setAuthType(AuthType.LOCAL_ONLY);
        target.setIsActive(true);
        target.setIsAccountApproved(true);
        target.setTotalLeaveDaysPerYear(BigDecimal.valueOf(20));
        target.setRemainingLeaveDays(BigDecimal.valueOf(20));
        target = employeeRepository.save(target);

        EmployeeResponse result = employeeService.transferOwnership(owner.getId(), target.getId());

        assertTrue(result.getIsOwner());
        assertEquals(Role.SUPER_ADMIN.name(), result.getRole());

        Employee updatedOwner = employeeRepository.findById(owner.getId()).get();
        assertFalse(updatedOwner.getIsOwner());
    }

    @Test
    void transferOwnership_shouldFailIfNotOwner() {
        Employee nonOwner = new Employee();
        nonOwner.setName("Non Owner");
        nonOwner.setEmail("nonowner@test.com");
        nonOwner.setRole(Role.SUPER_ADMIN);
        nonOwner.setAuthType(AuthType.LOCAL_ONLY);
        nonOwner.setIsActive(true);
        nonOwner.setIsAccountApproved(true);
        nonOwner.setIsOwner(false);
        nonOwner.setTotalLeaveDaysPerYear(BigDecimal.valueOf(20));
        nonOwner.setRemainingLeaveDays(BigDecimal.valueOf(20));
        Employee savedNonOwner = employeeRepository.save(nonOwner);

        Employee target = new Employee();
        target.setName("Target");
        target.setEmail("t2@test.com");
        target.setRole(Role.FIELD_EMPLOYEE);
        target.setAuthType(AuthType.LOCAL_ONLY);
        target.setIsActive(true);
        target.setIsAccountApproved(true);
        Employee savedTarget = employeeRepository.save(target);

        Long nonOwnerId = savedNonOwner.getId();
        Long targetId = savedTarget.getId();
        assertThrows(RuntimeException.class,
                () -> employeeService.transferOwnership(nonOwnerId, targetId));
    }

    @Test
    void deleteEmployee_shouldDeactivate() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("delete@test.com");
        req.setName("Delete Me");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setPassword("pass123");
        EmployeeResponse created = employeeService.createEmployee(req);

        employeeService.deleteEmployee(created.getId());

        Employee deactivated = employeeRepository.findById(created.getId()).get();
        assertFalse(deactivated.getIsActive());
    }

    @Test
    void createEmployee_shouldHashPassword() {
        EmployeeRequest req = new EmployeeRequest();
        req.setEmail("pwtest@test.com");
        req.setName("PW Test");
        req.setRole(Role.FIELD_EMPLOYEE);
        req.setPassword("secret123");

        employeeService.createEmployee(req);

        Employee saved = employeeRepository.findByEmail("pwtest@test.com").get();
        assertNotNull(saved.getPasswordHash());
        assertTrue(saved.getPasswordHash().startsWith("encoded:"));
    }
}
