package com.workflow.service;

import com.workflow.entity.AuditLog;
import com.workflow.entity.Employee;
import com.workflow.repository.AuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository alr) { this.auditLogRepository = alr; }

    public void log(String entityType, Long entityId, String action,
                    String previousStatus, String newStatus, String metadata) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType); log.setEntityId(entityId); log.setAction(action);
        log.setPreviousStatus(previousStatus); log.setNewStatus(newStatus); log.setMetadata(metadata);
        auditLogRepository.save(log);
    }

    public void logWithActor(String entityType, Long entityId, String action, Employee actor,
                             String previousStatus, String newStatus, String metadata) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType); log.setEntityId(entityId); log.setAction(action);
        log.setActor(actor); log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus); log.setMetadata(metadata);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogs(String entityType, String from, String to) {
        Specification<AuditLog> spec = Specification.where(null);
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (from != null && !from.isBlank()) {
            LocalDateTime fromDt = LocalDateTime.parse(from.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDt));
        }
        if (to != null && !to.isBlank()) {
            LocalDateTime toDt = LocalDateTime.parse(to.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDt));
        }
        return auditLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
