package com.workflow.repository;

import com.workflow.entity.RecycleBin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecycleBinRepository extends JpaRepository<RecycleBin, Long> {
    Page<RecycleBin> findByEntityTypeOrderByDeletedAtDesc(String entityType, Pageable pageable);
    Page<RecycleBin> findAllByOrderByDeletedAtDesc(Pageable pageable);
    List<RecycleBin> findByEntityTypeAndEntityId(String entityType, Long entityId);
    long countByEntityType(String entityType);
    long count();
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
