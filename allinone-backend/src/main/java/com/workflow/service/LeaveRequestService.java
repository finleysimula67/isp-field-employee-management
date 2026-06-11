package com.workflow.service;

import com.workflow.dto.LeaveRequestRequest;
import com.workflow.dto.LeaveReviewRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public LeaveRequestService(LeaveRequestRepository lrr, EmployeeRepository er,
                               NotificationRepository nr, AuditLogService als, EmailService emailService,
                               NotificationService notificationService) {
        this.leaveRequestRepository = lrr; this.employeeRepository = er;
        this.notificationRepository = nr; this.auditLogService = als; this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public List<LeaveRequest> getLeaveRequests(Long employeeId, String status) {
        List<LeaveRequest> requests;
        if (employeeId != null) {
            requests = leaveRequestRepository.findByEmployeeIdOrderBySubmittedAtDesc(employeeId);
        } else {
            requests = leaveRequestRepository.findAll(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "submittedAt"));
        }
        if (status != null)
            requests = requests.stream().filter(r -> r.getStatus().name().equals(status)).collect(Collectors.toList());
        return requests;
    }

    public LeaveRequest getLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
    }

    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequestRequest request, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        int durationDays = (int) Period.between(startDate, endDate).getDays() + 1;
        if (employee.getRemainingLeaveDays().compareTo(BigDecimal.valueOf(durationDays)) < 0)
            throw new RuntimeException("Insufficient remaining leave days");
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(LeaveType.valueOf(request.getLeaveType()));
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setDurationDays(durationDays);
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        auditLogService.log("LeaveRequest", saved.getId(), "CREATED", null, "PENDING", employee.getEmail());

        List<Employee> admins = employeeRepository.findByRoleIn(
                List.of(Role.SUPER_ADMIN, Role.BRANCH_MANAGER));
        for (Employee admin : admins) {
            if (admin.getId().equals(employee.getId())) continue;
            Notification adminNotif = new Notification();
            adminNotif.setRecipient(admin);
            adminNotif.setType("LEAVE_REQUESTED");
            adminNotif.setTitle("New Leave: " + employee.getName());
            adminNotif.setBody(durationDays + " day(s) " + request.getLeaveType() + " (" + startDate + " to " + endDate + ")");
            adminNotif.setRelatedEntityType("LeaveRequest");
            adminNotif.setRelatedEntityId(saved.getId());
            notificationRepository.save(adminNotif);
            notificationService.broadcastNotificationToRecipient(adminNotif);
            emailService.sendEmail(admin.getEmail(), "New Leave Request: " + employee.getName(),
                    employee.getName() + " requested " + durationDays + " day(s) of "
                    + request.getLeaveType() + " leave (" + startDate + " to " + endDate + ").\n\n"
                    + "Reason: " + (request.getReason() != null ? request.getReason() : "N/A"));
        }

        return saved;
    }

    @Transactional
    public LeaveRequest reviewLeaveRequest(Long id, LeaveReviewRequest request, Long reviewerId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (leaveRequest.getStatus() != LeaveStatus.PENDING)
            throw new RuntimeException("Leave request is not in PENDING status");
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        LeaveStatus newStatus = LeaveStatus.valueOf(request.getStatus());
        leaveRequest.setStatus(newStatus);
        leaveRequest.setReviewComment(request.getReviewComment());
        leaveRequest.setReviewedBy(reviewer);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        if (newStatus == LeaveStatus.APPROVED) {
            leaveRequest.setDeductedFromBalance(true);
            Employee emp = leaveRequest.getEmployee();
            emp.setRemainingLeaveDays(emp.getRemainingLeaveDays().subtract(
                    BigDecimal.valueOf(leaveRequest.getDurationDays())));
            employeeRepository.save(emp);
        }
        Notification notification = new Notification();
        notification.setRecipient(leaveRequest.getEmployee());
        notification.setType("LEAVE_REVIEW");
        notification.setTitle("Leave Request " + newStatus.name());
        notification.setBody(request.getReviewComment());
        notification.setRelatedEntityType("LeaveRequest");
        notification.setRelatedEntityId(leaveRequest.getId());
        notificationRepository.save(notification);
        notificationService.broadcastNotificationToRecipient(notification);
        emailService.sendEmail(leaveRequest.getEmployee().getEmail(), "Leave Request " + newStatus.name(),
                "Your leave request (" + leaveRequest.getStartDate() + " to " + leaveRequest.getEndDate()
                + ") has been " + newStatus.name() + ".\n\nComment: "
                + (request.getReviewComment() != null ? request.getReviewComment() : "N/A"));
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        auditLogService.log("LeaveRequest", id, "REVIEWED", "PENDING", newStatus.name(), reviewer.getEmail());
        return saved;
    }

    @Transactional
    public List<LeaveRequest> batchReviewLeaveRequests(List<Long> ids, LeaveReviewRequest request, Long reviewerId) {
        List<LeaveRequest> requests = leaveRequestRepository.findByIdIn(ids);
        for (LeaveRequest lr : requests) {
            reviewLeaveRequest(lr.getId(), request, reviewerId);
        }
        return leaveRequestRepository.findByIdIn(ids);
    }

}
