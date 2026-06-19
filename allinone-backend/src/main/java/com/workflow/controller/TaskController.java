package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.Task;
import com.workflow.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAll(
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) String status) {
        List<TaskResponse> list = taskService.getTasks(assignedTo, status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMy(
            @AuthenticationPrincipal Employee employee,
            @RequestParam(required = false) String status) {
        List<TaskResponse> list = taskService.getMyTasks(employee.getId(), status).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(taskService.getTask(id))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> create(@RequestBody TaskRequest request,
                                                             @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Task created", toResponse(taskService.createTask(request, employee.getId()))));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(@PathVariable Long id,
                                                                    @RequestBody TaskStatusUpdateRequest request,
                                                                    @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(taskService.updateTaskStatus(id, request, employee.getId()))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable Long id,
                                                             @RequestBody TaskRequest request,
                                                             @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(ApiResponse.ok("Task updated", toResponse(taskService.updateTask(id, request, employee.getId()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal Employee employee) {
        taskService.softDeleteTask(id, employee);
        return ResponseEntity.ok(ApiResponse.ok("Task deleted", null));
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody BatchDeleteRequest request,
                                                          @AuthenticationPrincipal Employee employee) {
        taskService.batchDeleteTasks(request.getIds(), employee);
        return ResponseEntity.ok(ApiResponse.ok("Batch delete completed", null));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse r = new TaskResponse();
        r.setId(task.getId());
        r.setAssignedBy(task.getAssignedBy() != null ? task.getAssignedBy().getId() : null);
        r.setAssignedByName(task.getAssignedBy() != null ? task.getAssignedBy().getName() : null);
        r.setAssignedTo(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null);
        r.setAssignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getName() : null);
        r.setTitle(task.getTitle());
        r.setDescription(task.getDescription());
        r.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        r.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        r.setScheduledDate(task.getScheduledDate() != null ? task.getScheduledDate().toString() : null);
        r.setCustomerName(task.getCustomerName());
        r.setCustomerPhone(task.getCustomerPhone());
        r.setCustomerAddress(task.getCustomerAddress());
        r.setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        r.setCompletedAt(task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
        return r;
    }
}
