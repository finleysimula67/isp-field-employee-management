package com.workflow.repository;

import com.workflow.entity.RecycleBin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecycleBinRepository extends JpaRepository<RecycleBin, Long> {
    Page<RecycleBin> findByEntityTypeOrderByDeletedAtDesc(String entityType, Pageable pageable);
    Page<RecycleBin> findAllByOrderByDeletedAtDesc(Pageable pageable);
    List<RecycleBin> findByEntityTypeAndEntityId(String entityType, Long entityId);
    long countByEntityType(String entityType);
    long count();
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);

    @Modifying
    @Query(value = "UPDATE recycle_bin SET deleted_by_id = :targetId WHERE deleted_by_id = :sourceId", nativeQuery = true)
    int transferDeletedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE recycle_bin SET restored_by_id = :targetId WHERE restored_by_id = :sourceId", nativeQuery = true)
    int transferRestoredBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
