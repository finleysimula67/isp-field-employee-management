package com.workflow.controller;

import com.workflow.dto.*;
import com.workflow.entity.Employee;
import com.workflow.entity.Holiday;
import com.workflow.service.HolidayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {
    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) { this.holidayService = holidayService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getAll() {
        List<HolidayResponse> list = holidayService.getHolidays().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HolidayResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(holidayService.getHoliday(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<HolidayResponse>> create(@RequestBody HolidayRequest request,
                                                                @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Holiday created",
                toResponse(holidayService.createHoliday(request, employee.getId()))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal Employee employee) {
        holidayService.softDeleteHoliday(id, employee);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody BatchDeleteRequest request,
                                                          @AuthenticationPrincipal Employee employee) {
        holidayService.batchDeleteHolidays(request.getIds(), employee);
        return ResponseEntity.ok(ApiResponse.ok("Batch delete completed", null));
    }

    private HolidayResponse toResponse(Holiday h) {
        HolidayResponse r = new HolidayResponse();
        r.setId(h.getId());
        r.setDate(h.getDate() != null ? h.getDate().toString() : null);
        r.setName(h.getName());
        r.setIsRecurringYearly(h.getIsRecurringYearly() != null ? h.getIsRecurringYearly() : false);
        r.setOvertimeApplies(h.getOvertimeApplies() != null ? h.getOvertimeApplies() : false);
        r.setCreatedBy(h.getCreatedBy() != null ? h.getCreatedBy().getId() : null);
        r.setCreatedByName(h.getCreatedBy() != null ? h.getCreatedBy().getName() : null);
        return r;
    }
}
