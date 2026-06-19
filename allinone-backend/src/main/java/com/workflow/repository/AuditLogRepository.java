package com.workflow.repository;

import com.workflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    @Query("SELECT a FROM AuditLog a WHERE a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
