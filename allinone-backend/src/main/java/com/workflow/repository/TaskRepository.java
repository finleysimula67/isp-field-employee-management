package com.workflow.repository;

import com.workflow.entity.Task;
import com.workflow.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Task> findAllWithEager();

    @Query(value = "SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo WHERE t.deletedAt IS NULL",
           countQuery = "SELECT COUNT(t) FROM Task t WHERE t.deletedAt IS NULL")
    Page<Task> findAllWithEager(Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo WHERE t.assignedTo.id = :assignedToId AND t.deletedAt IS NULL ORDER BY t.scheduledDate ASC")
    List<Task> findByAssignedToIdOrderByScheduledDateAsc(@Param("assignedToId") Long assignedToId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo WHERE t.assignedBy.id = :assignedById AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Task> findByAssignedByIdOrderByCreatedAtDesc(@Param("assignedById") Long assignedById);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo " +
           "WHERE t.deletedAt IS NULL " +
           "AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "ORDER BY t.createdAt DESC")
    Page<Task> findFiltered(@Param("assignedToId") Long assignedToId,
                            @Param("status") TaskStatus status,
                            Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo WHERE t.id = :id AND t.deletedAt IS NULL")
    java.util.Optional<Task> findByIdWithEager(@Param("id") Long id);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :assignedToId AND t.status = :status AND t.deletedAt IS NULL")
    long countByAssignedToIdAndStatus(@Param("assignedToId") Long assignedToId, @Param("status") TaskStatus status);

    @Modifying
    @Query(value = "UPDATE tasks SET assigned_to = :targetId WHERE assigned_to = :sourceId", nativeQuery = true)
    int transferAssignedTo(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE tasks SET assigned_by = :targetId WHERE assigned_by = :sourceId", nativeQuery = true)
    int transferAssignedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
