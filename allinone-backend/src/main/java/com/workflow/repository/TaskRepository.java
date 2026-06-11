package com.workflow.repository;

import com.workflow.entity.Task;
import com.workflow.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToIdOrderByScheduledDateAsc(Long assignedToId);
    List<Task> findByAssignedByIdOrderByCreatedAtDesc(Long assignedById);
    long countByStatus(TaskStatus status);
    long countByAssignedToIdAndStatus(Long assignedToId, TaskStatus status);
}
