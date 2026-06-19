package com.workflow.service;

import com.workflow.dto.HolidayRequest;
import com.workflow.entity.Employee;
import com.workflow.entity.Holiday;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.HolidayRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {
    private final HolidayRepository holidayRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final RecycleBinService recycleBinService;

    public HolidayService(HolidayRepository holidayRepository, EmployeeRepository employeeRepository,
                          AuditLogService auditLogService, RecycleBinService rbs) {
        this.holidayRepository = holidayRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService; this.recycleBinService = rbs;
    }

    public List<Holiday> getHolidays() {
        return holidayRepository.findAll(Sort.by(Sort.Direction.ASC, "date"));
    }

    public Holiday getHoliday(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
    }

    @Transactional
    public Holiday createHoliday(HolidayRequest request, Long createdById) {
        Employee creator = employeeRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Holiday holiday = new Holiday();
        holiday.setDate(LocalDate.parse(request.getDate()));
        holiday.setName(request.getName());
        if (request.getIsRecurringYearly() != null) holiday.setIsRecurringYearly(request.getIsRecurringYearly());
        if (request.getOvertimeApplies() != null) holiday.setOvertimeApplies(request.getOvertimeApplies());
        holiday.setCreatedBy(creator);
        Holiday saved = holidayRepository.save(holiday);
        auditLogService.log("Holiday", saved.getId(), "CREATED", null, "ACTIVE", creator.getEmail());
        return saved;
    }

    @Transactional
    public void softDeleteHoliday(Long id, Employee actor) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        holiday.setDeletedAt(java.time.LocalDateTime.now());
        holiday.setDeletedBy(actor.getId());
        holidayRepository.save(holiday);
        recycleBinService.softDelete(holiday, id, "Holiday", actor, null, null);
        auditLogService.log("Holiday", id, "SOFT_DELETED", "ACTIVE", "DELETED", actor.getEmail());
    }

    @Transactional
    public void batchDeleteHolidays(List<Long> ids, Employee actor) {
        for (Long id : ids) { try { softDeleteHoliday(id, actor); } catch (Exception e) { /* skip */ } }
        recycleBinService.bulkDeleteLogged("Holiday", ids.size(), actor);
    }
}
