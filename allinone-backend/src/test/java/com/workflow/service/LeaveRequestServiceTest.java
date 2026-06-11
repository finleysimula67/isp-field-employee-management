package com.workflow.service;

import com.workflow.dto.LeaveRequestRequest;
import com.workflow.dto.LeaveReviewRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LeaveRequestServiceTest {

    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationRepository notificationRepository;

    private LeaveRequestService leaveRequestService;
    private Employee employee;
    private Employee admin;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        notificationRepository.deleteAll();

        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };
        EmailService emailService = new EmailService(null) {
            @Override public void sendEmail(String to, String subject, String body) {}
            @Override public void sendPasswordResetEmail(String to, String resetLink) {}
        };
        NotificationService notificationService = new NotificationService(notificationRepository, employeeRepository, null) {
            @Override public void broadcastNotificationToRecipient(Notification notification) {}
            @Override public void broadcastNotification(Long recipientId, Notification notification) {}
            @Override public void broadcastUnreadCount(Long recipientId) {}
        };

        leaveRequestService = new LeaveRequestService(leaveRequestRepository, employeeRepository,
                notificationRepository, auditLogService, emailService, notificationService);

        employee = new Employee();
        employee.setName("Field Employee");
        employee.setEmail("emp@test.com");
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee.setIsAccountApproved(true);
        employee.setTotalLeaveDaysPerYear(BigDecimal.valueOf(20));
        employee.setRemainingLeaveDays(BigDecimal.valueOf(15));
        employee = employeeRepository.save(employee);

        admin = new Employee();
        admin.setName("Super Admin");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setAuthType(AuthType.LOCAL_ONLY);
        admin.setIsActive(true);
        admin.setIsAccountApproved(true);
        admin = employeeRepository.save(admin);
    }

    @Test
    void createLeaveRequest_shouldSucceed() {
        LeaveRequest result = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(3, result.getDurationDays());
    }

    @Test
    void createLeaveRequest_shouldFailForInsufficientLeaveDays() {
        employee.setRemainingLeaveDays(BigDecimal.valueOf(1));
        employeeRepository.save(employee);
        assertThrows(RuntimeException.class,
                () -> leaveRequestService.createLeaveRequest(createRequest(), employee.getId()));
    }

    @Test
    void createLeaveRequest_shouldNotifyAdmins() {
        leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        List<Notification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.stream().allMatch(n -> n.getType().equals("LEAVE_REQUESTED")));
    }

    @Test
    void getLeaveRequest_shouldReturn() {
        LeaveRequest created = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        LeaveRequest found = leaveRequestService.getLeaveRequest(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getLeaveRequest_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> leaveRequestService.getLeaveRequest(999L));
    }

    @Test
    void getLeaveRequests_shouldFilterByEmployee() {
        leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        List<LeaveRequest> requests = leaveRequestService.getLeaveRequests(employee.getId(), null);
        assertEquals(1, requests.size());
    }

    @Test
    void reviewLeaveRequest_shouldApproveAndDeductRemainingDays() {
        LeaveRequest created = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        LeaveReviewRequest reviewReq = new LeaveReviewRequest();
        reviewReq.setStatus("APPROVED");
        reviewReq.setReviewComment("Approved");
        LeaveRequest reviewed = leaveRequestService.reviewLeaveRequest(created.getId(), reviewReq, admin.getId());
        assertEquals(LeaveStatus.APPROVED, reviewed.getStatus());
        assertTrue(reviewed.getDeductedFromBalance());
        Employee updated = employeeRepository.findById(employee.getId()).get();
        assertEquals(0, BigDecimal.valueOf(12).compareTo(updated.getRemainingLeaveDays()));
    }

    @Test
    void reviewLeaveRequest_shouldReject() {
        LeaveRequest created = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        LeaveReviewRequest reviewReq = new LeaveReviewRequest();
        reviewReq.setStatus("REJECTED");
        reviewReq.setReviewComment("Not approved");
        LeaveRequest reviewed = leaveRequestService.reviewLeaveRequest(created.getId(), reviewReq, admin.getId());
        assertEquals(LeaveStatus.REJECTED, reviewed.getStatus());
        assertFalse(reviewed.getDeductedFromBalance());
    }

    @Test
    void reviewLeaveRequest_shouldFailForNonPending() {
        LeaveRequest created = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        LeaveReviewRequest reviewReq = new LeaveReviewRequest();
        reviewReq.setStatus("APPROVED");
        leaveRequestService.reviewLeaveRequest(created.getId(), reviewReq, admin.getId());
        assertThrows(RuntimeException.class,
                () -> leaveRequestService.reviewLeaveRequest(created.getId(), reviewReq, admin.getId()));
    }

    @Test
    void batchReviewLeaveRequests_shouldReviewMultiple() {
        LeaveRequest lr1 = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        LeaveRequest lr2 = leaveRequestService.createLeaveRequest(createRequest(), employee.getId());
        LeaveReviewRequest reviewReq = new LeaveReviewRequest();
        reviewReq.setStatus("APPROVED");
        reviewReq.setReviewComment("Batch");
        List<LeaveRequest> reviewed = leaveRequestService.batchReviewLeaveRequests(
                List.of(lr1.getId(), lr2.getId()), reviewReq, admin.getId());
        assertEquals(2, reviewed.size());
        assertTrue(reviewed.stream().allMatch(l -> l.getStatus() == LeaveStatus.APPROVED));
    }

    private LeaveRequestRequest createRequest() {
        LeaveRequestRequest req = new LeaveRequestRequest();
        req.setLeaveType("ANNUAL");
        req.setStartDate(LocalDate.now().plusDays(5).toString());
        req.setEndDate(LocalDate.now().plusDays(7).toString());
        req.setReason("Family event");
        return req;
    }
}
