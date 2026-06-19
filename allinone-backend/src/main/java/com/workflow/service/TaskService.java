package com.workflow.service;

import com.workflow.dto.TaskRequest;
import com.workflow.dto.TaskStatusUpdateRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final RecycleBinService recycleBinService;

    public TaskService(TaskRepository tr, EmployeeRepository er, NotificationRepository nr, AuditLogService als,
                       NotificationService notificationService, RecycleBinService rbs) {
        this.taskRepository = tr; this.employeeRepository = er; this.notificationRepository = nr; this.auditLogService = als;
        this.notificationService = notificationService; this.recycleBinService = rbs;
    }

    public List<Task> getTasks(Long assignedTo, String status) {
        List<Task> tasks;
        if (assignedTo != null) {
            tasks = taskRepository.findByAssignedToIdOrderByScheduledDateAsc(assignedTo);
        } else {
            tasks = taskRepository.findAllWithEager(
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE,
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))).getContent();
        }
        if (status != null)
            tasks = tasks.stream().filter(t -> t.getStatus().name().equals(status)).collect(Collectors.toList());
        return tasks;
    }

    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Transactional
    public Task createTask(TaskRequest request, Long assignedById) {
        Employee assignedTo = employeeRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new RuntimeException("Assigned employee not found"));
        Employee assignedBy = employeeRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));
        Task task = new Task();
        task.setAssignedBy(assignedBy);
        task.setAssignedTo(assignedTo);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(TaskPriority.valueOf(request.getPriority()));
        if (request.getScheduledDate() != null) task.setScheduledDate(LocalDate.parse(request.getScheduledDate()));
        task.setCustomerName(request.getCustomerName());
        task.setCustomerPhone(request.getCustomerPhone());
        task.setCustomerAddress(request.getCustomerAddress());
        task.setStatus(TaskStatus.OPEN);
        Task saved = taskRepository.save(task);
        auditLogService.log("Task", saved.getId(), "CREATED", null, "OPEN", assignedTo.getEmail());
        Notification notification = new Notification();
        notification.setRecipient(assignedTo);
        notification.setType("TASK_ASSIGNED");
        notification.setTitle("New Task: " + saved.getTitle());
        notification.setBody("You have been assigned a new task by " + assignedBy.getName());
        notification.setRelatedEntityType("Task");
        notification.setRelatedEntityId(saved.getId());
        notificationRepository.save(notification);
        notificationService.broadcastNotificationToRecipient(notification);
        return saved;
    }

    @Transactional
    public Task updateTaskStatus(Long id, TaskStatusUpdateRequest request, Long currentUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Employee currentUser = employeeRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAssigned = task.getAssignedTo().getId().equals(currentUserId);
        boolean isManagerOrAdmin = currentUser.getRole() == Role.BRANCH_MANAGER
                || currentUser.getRole() == Role.SUPER_ADMIN;
        if (!isAssigned && !isManagerOrAdmin)
            throw new AccessDeniedException("Not authorized to update this task");
        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = TaskStatus.valueOf(request.getStatus());
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.COMPLETED)
            task.setCompletedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        auditLogService.log("Task", id, "STATUS_CHANGED", oldStatus.name(), newStatus.name(), currentUser.getEmail());

        if (!isManagerOrAdmin) {
            List<Employee> admins = employeeRepository.findByRoleIn(
                    List.of(Role.SUPER_ADMIN, Role.BRANCH_MANAGER));
            for (Employee admin : admins) {
                if (admin.getId().equals(currentUserId)) continue;
                Notification adminNotif = new Notification();
                adminNotif.setRecipient(admin);
                adminNotif.setType("TASK_STATUS_UPDATED");
                adminNotif.setTitle("Task " + newStatus.name() + ": " + saved.getTitle());
                adminNotif.setBody(currentUser.getName() + " updated task status to " + newStatus.name());
                adminNotif.setRelatedEntityType("Task");
                adminNotif.setRelatedEntityId(saved.getId());
                notificationRepository.save(adminNotif);
                notificationService.broadcastNotificationToRecipient(adminNotif);
            }
        }

        return saved;
    }

    public List<Task> getMyTasks(Long employeeId, String status) {
        List<Task> tasks = taskRepository.findByAssignedToIdOrderByScheduledDateAsc(employeeId);
        if (status != null)
            tasks = tasks.stream().filter(t -> t.getStatus().name().equals(status)).collect(Collectors.toList());
        return tasks;
    }

    @Transactional
    public Task updateTask(Long id, TaskRequest request, Long currentUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (request.getAssignedTo() != null) {
            Employee assignedTo = employeeRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new RuntimeException("Assigned employee not found"));
            task.setAssignedTo(assignedTo);
        }
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(TaskPriority.valueOf(request.getPriority()));
        if (request.getScheduledDate() != null) task.setScheduledDate(LocalDate.parse(request.getScheduledDate()));
        if (request.getCustomerName() != null) task.setCustomerName(request.getCustomerName());
        if (request.getCustomerPhone() != null) task.setCustomerPhone(request.getCustomerPhone());
        if (request.getCustomerAddress() != null) task.setCustomerAddress(request.getCustomerAddress());
        Task saved = taskRepository.save(task);
        auditLogService.log("Task", id, "UPDATED", null, null, String.valueOf(currentUserId));
        return saved;
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);
        auditLogService.log("Task", id, "CANCELLED", null, "CANCELLED", null);
    }

    @Transactional
    public void softDeleteTask(Long id, Employee actor) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        boolean isAdmin = actor.getRole() == Role.SUPER_ADMIN || actor.getRole() == Role.BRANCH_MANAGER;
        boolean isOwner = task.getAssignedBy().getId().equals(actor.getId());
        if (!isAdmin && !isOwner)
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to delete this task");
        task.setDeletedAt(java.time.LocalDateTime.now());
        task.setDeletedBy(actor.getId());
        taskRepository.save(task);
        recycleBinService.softDelete(task, id, "Task", actor, task.getAssignedTo().getId(), task.getCreatedAt());
        auditLogService.log("Task", id, "SOFT_DELETED", task.getStatus().name(), "DELETED", actor.getEmail());
    }

    @Transactional
    public void batchDeleteTasks(List<Long> ids, Employee actor) {
        for (Long id : ids) { try { softDeleteTask(id, actor); } catch (Exception e) { /* skip */ } }
        recycleBinService.bulkDeleteLogged("Task", ids.size(), actor);
    }
}
