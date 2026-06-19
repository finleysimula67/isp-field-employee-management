package com.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.workflow.dto.BatchDeleteRequest;
import com.workflow.entity.Employee;
import com.workflow.entity.RecycleBin;
import com.workflow.repository.RecycleBinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class RecycleBinService {
    private final RecycleBinRepository recycleBinRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public RecycleBinService(RecycleBinRepository rbr, AuditLogService als) {
        this.recycleBinRepository = rbr;
        this.auditLogService = als;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Transactional
    public void softDelete(Object entity, Long entityId, String entityType,
                           Employee actor, Long originalOwnerId,
                           LocalDateTime originalCreatedAt) {
        RecycleBin bin = new RecycleBin();
        bin.setEntityType(entityType);
        bin.setEntityId(entityId);
        try {
            bin.setRecordData(objectMapper.writeValueAsString(entity));
        } catch (Exception e) {
            bin.setRecordData("{}");
        }
        bin.setDeletedBy(actor);
        bin.setOriginalOwnerId(originalOwnerId);
        bin.setOriginalCreatedAt(originalCreatedAt);
        recycleBinRepository.save(bin);
        auditLogService.logWithActor(entityType, entityId, "SOFT_DELETED", actor, null, "DELETED", null);
    }

    @Transactional
    public void restore(Long recycleBinId, Employee actor) {
        RecycleBin bin = recycleBinRepository.findById(recycleBinId)
                .orElseThrow(() -> new RuntimeException("Recycle bin entry not found"));
        bin.setRestoredAt(LocalDateTime.now());
        bin.setRestoredBy(actor);
        recycleBinRepository.save(bin);
        auditLogService.logWithActor(bin.getEntityType(), bin.getEntityId(), "RESTORED", actor, "DELETED", "ACTIVE", null);
    }

    @Transactional
    public void permanentDelete(Long recycleBinId, Employee actor) {
        RecycleBin bin = recycleBinRepository.findById(recycleBinId)
                .orElseThrow(() -> new RuntimeException("Recycle bin entry not found"));
        auditLogService.logWithActor(bin.getEntityType(), bin.getEntityId(),
                "PERMANENTLY_DELETED", actor, "DELETED", "DELETED_PERMANENT", null);
        recycleBinRepository.delete(bin);
    }

    @Transactional
    public void bulkDeleteLogged(String entityType, int count, Employee actor) {
        auditLogService.logWithActor(entityType, 0L, "BULK_DELETED", actor, null,
                "DELETED", String.valueOf(count) + " records");
    }

    public Page<RecycleBin> getAll(String entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "deletedAt");
        if (entityType != null && !entityType.isBlank())
            return recycleBinRepository.findByEntityTypeOrderByDeletedAtDesc(entityType, pageable);
        return recycleBinRepository.findAllByOrderByDeletedAtDesc(pageable);
    }

    public long count(String entityType) {
        if (entityType != null && !entityType.isBlank())
            return recycleBinRepository.countByEntityType(entityType);
        return recycleBinRepository.count();
    }
}
