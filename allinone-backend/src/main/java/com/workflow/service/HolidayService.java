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

    public HolidayService(HolidayRepository holidayRepository, EmployeeRepository employeeRepository,
                          AuditLogService auditLogService) {
        this.holidayRepository = holidayRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
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
    public void deleteHoliday(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        holidayRepository.delete(holiday);
        auditLogService.log("Holiday", id, "DELETED", "ACTIVE", "DELETED", null);
    }
}
