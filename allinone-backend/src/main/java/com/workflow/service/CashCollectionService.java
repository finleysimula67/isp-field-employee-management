package com.workflow.service;

import com.workflow.dto.CashCollectionRequest;
import com.workflow.dto.CashCollectionReviewRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashCollectionService {
    private final CashCollectionRepository cashCollectionRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public CashCollectionService(CashCollectionRepository ccr, EmployeeRepository er,
                                  NotificationRepository nr, AuditLogService als,
                                  EmailService emailService, NotificationService notificationService) {
        this.cashCollectionRepository = ccr; this.employeeRepository = er;
        this.notificationRepository = nr; this.auditLogService = als;
        this.emailService = emailService; this.notificationService = notificationService;
    }

    public List<CashCollection> getCashCollections(Long employeeId, String status) {
        List<CashCollection> collections = cashCollectionRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"));
        if (employeeId != null)
            collections = collections.stream().filter(c -> c.getEmployee().getId().equals(employeeId)).collect(Collectors.toList());
        if (status != null)
            collections = collections.stream().filter(c -> c.getStatus().name().equals(status)).collect(Collectors.toList());
        return collections;
    }

    public CashCollection getCashCollection(Long id) {
        return cashCollectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash collection not found"));
    }

    @Transactional
    public CashCollection createCashCollection(CashCollectionRequest request, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        CashCollection collection = new CashCollection();
        collection.setEmployee(employee);
        collection.setCustomerName(request.getCustomerName());
        collection.setCustomerPhone(request.getCustomerPhone());
        collection.setCustomerAddress(request.getCustomerAddress());
        collection.setAmount(BigDecimal.valueOf(request.getAmount()));
        collection.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        collection.setServiceType(ServiceType.valueOf(request.getServiceType()));
        collection.setDescription(request.getDescription());
        if (request.getLocationLat() != null) collection.setLocationLat(BigDecimal.valueOf(request.getLocationLat()));
        if (request.getLocationLng() != null) collection.setLocationLng(BigDecimal.valueOf(request.getLocationLng()));
        collection.setPhotoUrls(request.getPhotoUrls());
        collection.setStatus(CollectionStatus.PENDING);

        CashCollection saved = cashCollectionRepository.save(collection);
        auditLogService.log("CashCollection", saved.getId(), "CREATED", null, "PENDING", employee.getEmail());

        List<Employee> admins = employeeRepository.findByRoleIn(
                List.of(Role.SUPER_ADMIN, Role.BRANCH_MANAGER));
        for (Employee admin : admins) {
            if (admin.getId().equals(employee.getId())) continue;
            Notification notif = new Notification();
            notif.setRecipient(admin);
            notif.setType("CASH_COLLECTION_SUBMITTED");
            notif.setTitle("New Cash Collection from " + employee.getName());
            notif.setBody(request.getCustomerName() + " - " + request.getAmount());
            notif.setRelatedEntityType("CashCollection");
            notif.setRelatedEntityId(saved.getId());
            notificationRepository.save(notif);
            try {
                notificationService.broadcastNotificationToRecipient(notif);
            } catch (Exception e) {
                System.err.println("Cash collection notification broadcast skipped: " + e.getMessage());
            }

            try {
                emailService.sendEmail(admin.getEmail(), "New Cash Collection: " + employee.getName(),
                        employee.getName() + " collected " + request.getAmount()
                        + " from " + request.getCustomerName()
                        + ".\n\nDetails: " + (request.getDescription() != null ? request.getDescription() : "N/A"));
            } catch (Exception e) {
                System.err.println("Cash collection notification email skipped: " + e.getMessage());
            }
        }

        return saved;
    }

    @Transactional
    public CashCollection reviewCashCollection(Long id, CashCollectionReviewRequest request, Long reviewerId) {
        CashCollection collection = cashCollectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash collection not found"));
        if (collection.getStatus() != CollectionStatus.PENDING)
            throw new RuntimeException("Cash collection is not in PENDING status");
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        CollectionStatus newStatus = CollectionStatus.valueOf(request.getStatus());
        collection.setStatus(newStatus);
        collection.setReviewComment(request.getReviewComment());
        collection.setReviewedBy(reviewer);
        collection.setReviewedAt(LocalDateTime.now());

        CashCollection saved = cashCollectionRepository.save(collection);
        auditLogService.log("CashCollection", id, "REVIEWED", "PENDING", newStatus.name(), reviewer.getEmail());

        Notification notification = new Notification();
        notification.setRecipient(collection.getEmployee());
        notification.setType("CASH_COLLECTION_REVIEW");
        notification.setTitle("Cash Collection " + newStatus.name());
        notification.setBody(request.getReviewComment());
        notification.setRelatedEntityType("CashCollection");
        notification.setRelatedEntityId(saved.getId());
        notificationRepository.save(notification);
        notificationService.broadcastNotificationToRecipient(notification);

        try {
            emailService.sendEmail(collection.getEmployee().getEmail(), "Cash Collection " + newStatus.name(),
                    "Your cash collection for " + collection.getCustomerName()
                    + " (" + collection.getAmount() + ") has been " + newStatus.name()
                    + ".\n\nComment: " + (request.getReviewComment() != null ? request.getReviewComment() : "N/A"));
        } catch (Exception e) {
            System.err.println("Cash collection review email skipped: " + e.getMessage());
        }

        return saved;
    }

    public List<CashCollection> batchReviewCashCollections(List<Long> ids, CashCollectionReviewRequest request, Long reviewerId) {
        List<CashCollection> collections = cashCollectionRepository.findByIdIn(ids);
        for (CashCollection c : collections) {
            reviewCashCollection(c.getId(), request, reviewerId);
        }
        return cashCollectionRepository.findByIdIn(ids);
    }

    public List<CashCollection> getMyCashCollections(Long employeeId, String status) {
        List<CashCollection> collections = cashCollectionRepository.findByEmployeeIdOrderBySubmittedAtDesc(employeeId);
        if (status != null)
            collections = collections.stream().filter(c -> c.getStatus().name().equals(status)).collect(Collectors.toList());
        return collections;
    }
}
