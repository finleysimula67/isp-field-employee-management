package com.workflow.service;

import com.workflow.dto.TaskRequest;
import com.workflow.dto.TaskStatusUpdateRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired private TaskRepository taskRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationRepository notificationRepository;

    private TaskService taskService;
    private Employee employee;
    private Employee manager;
    private Employee otherEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        taskRepository.deleteAll();
        notificationRepository.deleteAll();

        AuditLogService auditLogService = new AuditLogService(null) {
            @Override public void log(String entityType, Long entityId, String action, String previousStatus, String newStatus, String metadata) {}
        };
        NotificationService notificationService = new NotificationService(notificationRepository, employeeRepository, null) {
            @Override public void broadcastNotificationToRecipient(Notification notification) {}
            @Override public void broadcastNotification(Long recipientId, Notification notification) {}
            @Override public void broadcastUnreadCount(Long recipientId) {}
        };
        RecycleBinService recycleBinService = new RecycleBinService(null, null) {
            @Override public void softDelete(Object entity, Long entityId, String entityType, Employee actor, Long originalOwnerId, java.time.LocalDateTime originalCreatedAt) {}
            @Override public void bulkDeleteLogged(String entityType, int count, Employee actor) {}
        };

        taskService = new TaskService(taskRepository, employeeRepository, notificationRepository, auditLogService, notificationService, recycleBinService);

        employee = new Employee();
        employee.setName("Field Employee");
        employee.setEmail("emp@test.com");
        employee.setRole(Role.FIELD_EMPLOYEE);
        employee.setAuthType(AuthType.LOCAL_ONLY);
        employee.setIsActive(true);
        employee.setIsAccountApproved(true);
        employee = employeeRepository.save(employee);

        manager = new Employee();
        manager.setName("Branch Manager");
        manager.setEmail("mgr@test.com");
        manager.setRole(Role.BRANCH_MANAGER);
        manager.setAuthType(AuthType.LOCAL_ONLY);
        manager.setIsActive(true);
        manager.setIsAccountApproved(true);
        manager = employeeRepository.save(manager);

        otherEmployee = new Employee();
        otherEmployee.setName("Other Employee");
        otherEmployee.setEmail("other@test.com");
        otherEmployee.setRole(Role.FIELD_EMPLOYEE);
        otherEmployee.setAuthType(AuthType.LOCAL_ONLY);
        otherEmployee.setIsActive(true);
        otherEmployee.setIsAccountApproved(true);
        otherEmployee = employeeRepository.save(otherEmployee);
    }

    @Test
    void createTask_shouldCreateOpenTask() {
        TaskRequest req = createRequest(employee.getId());
        Task result = taskService.createTask(req, manager.getId());
        assertEquals(TaskStatus.OPEN, result.getStatus());
        assertEquals(employee.getId(), result.getAssignedTo().getId());
        assertEquals(manager.getId(), result.getAssignedBy().getId());
    }

    @Test
    void createTask_shouldNotifyAssignedEmployee() {
        TaskRequest req = createRequest(employee.getId());
        taskService.createTask(req, manager.getId());
        List<Notification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.stream().anyMatch(n -> n.getType().equals("TASK_ASSIGNED")));
    }

    @Test
    void getTask_shouldReturn() {
        TaskRequest req = createRequest(employee.getId());
        Task created = taskService.createTask(req, manager.getId());
        Task found = taskService.getTask(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getTask_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> taskService.getTask(999L));
    }

    @Test
    void getTasks_shouldFilterByAssignedTo() {
        taskService.createTask(createRequest(employee.getId()), manager.getId());
        List<Task> tasks = taskService.getTasks(employee.getId(), null);
        assertEquals(1, tasks.size());
    }

    @Test
    void updateTaskStatus_assignedEmployeeCanUpdate() {
        Task task = taskService.createTask(createRequest(employee.getId()), manager.getId());
        TaskStatusUpdateRequest updateReq = new TaskStatusUpdateRequest();
        updateReq.setStatus("IN_PROGRESS");
        Task updated = taskService.updateTaskStatus(task.getId(), updateReq, employee.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void updateTaskStatus_managerCanUpdate() {
        Task task = taskService.createTask(createRequest(employee.getId()), manager.getId());
        TaskStatusUpdateRequest updateReq = new TaskStatusUpdateRequest();
        updateReq.setStatus("COMPLETED");
        Task updated = taskService.updateTaskStatus(task.getId(), updateReq, manager.getId());
        assertEquals(TaskStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());
    }

    @Test
    void updateTaskStatus_unauthorizedUserShouldThrow() {
        Task task = taskService.createTask(createRequest(employee.getId()), manager.getId());
        TaskStatusUpdateRequest updateReq = new TaskStatusUpdateRequest();
        updateReq.setStatus("IN_PROGRESS");
        assertThrows(RuntimeException.class,
                () -> taskService.updateTaskStatus(task.getId(), updateReq, otherEmployee.getId()));
    }

    @Test
    void getMyTasks_shouldReturnAssigned() {
        taskService.createTask(createRequest(employee.getId()), manager.getId());
        List<Task> tasks = taskService.getMyTasks(employee.getId(), null);
        assertEquals(1, tasks.size());
    }

    @Test
    void getMyTasks_shouldFilterByStatus() {
        taskService.createTask(createRequest(employee.getId()), manager.getId());
        List<Task> open = taskService.getMyTasks(employee.getId(), "OPEN");
        assertEquals(1, open.size());
        List<Task> completed = taskService.getMyTasks(employee.getId(), "COMPLETED");
        assertTrue(completed.isEmpty());
    }

    private TaskRequest createRequest(Long assignedToId) {
        TaskRequest req = new TaskRequest();
        req.setAssignedTo(assignedToId);
        req.setTitle("Test Task");
        req.setDescription("Test description");
        req.setPriority("HIGH");
        return req;
    }
}
