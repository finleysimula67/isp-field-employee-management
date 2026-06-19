package com.workflow.repository;

import com.workflow.entity.Task;
import com.workflow.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :assignedToId AND t.deletedAt IS NULL ORDER BY t.scheduledDate ASC")
    List<Task> findByAssignedToIdOrderByScheduledDateAsc(@Param("assignedToId") Long assignedToId);

    @Query("SELECT t FROM Task t WHERE t.assignedBy.id = :assignedById AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Task> findByAssignedByIdOrderByCreatedAtDesc(@Param("assignedById") Long assignedById);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :assignedToId AND t.status = :status AND t.deletedAt IS NULL")
    long countByAssignedToIdAndStatus(@Param("assignedToId") Long assignedToId, @Param("status") TaskStatus status);
}
