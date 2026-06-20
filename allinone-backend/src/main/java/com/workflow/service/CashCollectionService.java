package com.workflow.service;

import com.workflow.dto.*;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashCollectionService {
    private final CashCollectionRepository cashCollectionRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final RecycleBinService recycleBinService;

    public CashCollectionService(CashCollectionRepository ccr, EmployeeRepository er,
                                  NotificationRepository nr, AuditLogService als,
                                  EmailService emailService, NotificationService notificationService,
                                  RecycleBinService rbs) {
        this.cashCollectionRepository = ccr; this.employeeRepository = er;
        this.notificationRepository = nr; this.auditLogService = als;
        this.emailService = emailService; this.notificationService = notificationService;
        this.recycleBinService = rbs;
    }

    public List<CashCollection> getCashCollections(Long employeeId, String status, int page, int size) {
        CollectionStatus statusEnum = status != null ? CollectionStatus.valueOf(status) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "submittedAt");
        return cashCollectionRepository.findFiltered(employeeId, statusEnum, pageable).getContent();
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

        if (request.getAmount() != null) collection.setAmount(BigDecimal.valueOf(request.getAmount()));
        if (request.getCustomerName() != null) collection.setCustomerName(request.getCustomerName());
        if (request.getCustomerPhone() != null) collection.setCustomerPhone(request.getCustomerPhone());
        if (request.getCustomerAddress() != null) collection.setCustomerAddress(request.getCustomerAddress());
        if (request.getDescription() != null) collection.setDescription(request.getDescription());
        if (request.getPaymentMethod() != null) collection.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        if (request.getServiceType() != null) collection.setServiceType(ServiceType.valueOf(request.getServiceType()));

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

    @Transactional
    public void softDeleteCashCollection(Long id, Employee actor) {
        CashCollection collection = cashCollectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash collection not found"));
        boolean isOwner = collection.getEmployee().getId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == Role.SUPER_ADMIN || actor.getRole() == Role.BRANCH_MANAGER;
        if (!isAdmin && (!isOwner || collection.getStatus() != CollectionStatus.PENDING))
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to delete this collection");
        collection.setDeletedAt(java.time.LocalDateTime.now());
        collection.setDeletedBy(actor.getId());
        cashCollectionRepository.save(collection);
        recycleBinService.softDelete(collection, id, "CashCollection", actor, collection.getEmployee().getId(), collection.getSubmittedAt());
        auditLogService.log("CashCollection", id, "SOFT_DELETED", collection.getStatus().name(), "DELETED", actor.getEmail());
    }

    @Transactional
    public void batchDeleteCashCollections(List<Long> ids, Employee actor) {
        for (Long id : ids) {
            try { softDeleteCashCollection(id, actor); } catch (Exception e) { /* skip */ }
        }
        recycleBinService.bulkDeleteLogged("CashCollection", ids.size(), actor);
    }

    public List<CashCollection> batchReviewCashCollections(List<Long> ids, CashCollectionReviewRequest request, Long reviewerId) {
        List<CashCollection> collections = cashCollectionRepository.findByIdInWithEager(ids);
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        CollectionStatus newStatus = CollectionStatus.valueOf(request.getStatus());

        for (CashCollection c : collections) {
            if (c.getStatus() != CollectionStatus.PENDING) continue;
            c.setStatus(newStatus);
            c.setReviewComment(request.getReviewComment());
            c.setReviewedBy(reviewer);
            c.setReviewedAt(LocalDateTime.now());
            cashCollectionRepository.save(c);
            auditLogService.log("CashCollection", c.getId(), "REVIEWED", "PENDING", newStatus.name(), reviewer.getEmail());

            Notification notification = new Notification();
            notification.setRecipient(c.getEmployee());
            notification.setType("CASH_COLLECTION_REVIEW");
            notification.setTitle("Cash Collection " + newStatus.name());
            notification.setBody(request.getReviewComment());
            notification.setRelatedEntityType("CashCollection");
            notification.setRelatedEntityId(c.getId());
            notificationRepository.save(notification);
            notificationService.broadcastNotificationToRecipient(notification);

            try {
                emailService.sendEmail(c.getEmployee().getEmail(), "Cash Collection " + newStatus.name(),
                        "Your cash collection for " + c.getCustomerName()
                        + " (" + c.getAmount() + ") has been " + newStatus.name()
                        + ".\n\nComment: " + (request.getReviewComment() != null ? request.getReviewComment() : "N/A"));
            } catch (Exception e) {
                System.err.println("Cash collection review email skipped: " + e.getMessage());
            }
        }
        return cashCollectionRepository.findByIdInWithEager(ids);
    }

    public List<CashCollection> getMyCashCollections(Long employeeId, String status) {
        List<CashCollection> collections = cashCollectionRepository.findByEmployeeIdWithEager(employeeId);
        if (status != null)
            collections = collections.stream().filter(c -> c.getStatus().name().equals(status)).collect(Collectors.toList());
        return collections;
    }

    @Transactional
    public CashCollection createCashCollectionByAdmin(AdminCashCollectionRequest request, Long adminId) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Employee admin = employeeRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
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
        collection.setReviewedBy(admin);

        CashCollection saved = cashCollectionRepository.save(collection);
        auditLogService.log("CashCollection", saved.getId(), "CREATED_BY_ADMIN", null, "PENDING", admin.getEmail());

        Notification notif = new Notification();
        notif.setRecipient(employee);
        notif.setType("CASH_COLLECTION_CREATED");
        notif.setTitle("Cash Collection recorded by " + admin.getName());
        notif.setBody(request.getCustomerName() + " - Rs. " + request.getAmount());
        notif.setRelatedEntityType("CashCollection");
        notif.setRelatedEntityId(saved.getId());
        notificationRepository.save(notif);
        notificationService.broadcastNotificationToRecipient(notif);

        return saved;
    }

    public List<CashCollectionSummaryResponse> getMonthlySummary(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<CashCollection> collections = cashCollectionRepository.findBySubmittedAtBetweenWithEager(start, end);
        Map<Long, List<CashCollection>> byEmployee = collections.stream()
                .collect(Collectors.groupingBy(c -> c.getEmployee().getId()));

        List<CashCollectionSummaryResponse> result = new ArrayList<>();
        for (Map.Entry<Long, List<CashCollection>> entry : byEmployee.entrySet()) {
            Employee emp = entry.getValue().get(0).getEmployee();
            CashCollectionSummaryResponse summary = buildSummary(emp, entry.getValue());
            result.add(summary);
        }
        result.sort(Comparator.comparing(CashCollectionSummaryResponse::getEmployeeName));
        return result;
    }

    public CashCollectionSummaryResponse getMyMonthlySummary(Long employeeId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<CashCollection> collections = cashCollectionRepository.findBySubmittedAtBetweenWithEager(start, end).stream()
                .filter(c -> c.getEmployee().getId().equals(employeeId))
                .collect(Collectors.toList());
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return buildSummary(emp, collections);
    }

    private CashCollectionSummaryResponse buildSummary(Employee emp, List<CashCollection> collections) {
        CashCollectionSummaryResponse summary = new CashCollectionSummaryResponse();
        summary.setEmployeeId(emp.getId());
        summary.setEmployeeName(emp.getName());

        Map<Integer, List<DayCollectionEntry>> daysMap = new HashMap<>();
        double totalCollected = 0, totalPending = 0, totalRejected = 0, totalSubmitted = 0;
        int approvedCount = 0, pendingCount = 0;

        for (CashCollection c : collections) {
            int day = c.getSubmittedAt().getDayOfMonth();
            DayCollectionEntry entry = new DayCollectionEntry();
            entry.setId(c.getId());
            entry.setAmount(c.getAmount().doubleValue());
            entry.setStatus(c.getStatus().name());
            entry.setCustomerName(c.getCustomerName());

            daysMap.computeIfAbsent(day, k -> new ArrayList<>()).add(entry);

            totalSubmitted += c.getAmount().doubleValue();
            switch (c.getStatus()) {
                case APPROVED:
                    totalCollected += c.getAmount().doubleValue();
                    approvedCount++;
                    break;
                case PENDING:
                    totalPending += c.getAmount().doubleValue();
                    pendingCount++;
                    break;
                case REJECTED:
                    totalRejected += c.getAmount().doubleValue();
                    break;
                default:
                    break;
            }
        }

        summary.setDays(daysMap);
        summary.setTotalCollected(totalCollected);
        summary.setTotalPending(totalPending);
        summary.setTotalRejected(totalRejected);
        summary.setTotalSubmitted(totalSubmitted);
        summary.setApprovedCount(approvedCount);
        summary.setPendingCount(pendingCount);
        return summary;
    }
}
