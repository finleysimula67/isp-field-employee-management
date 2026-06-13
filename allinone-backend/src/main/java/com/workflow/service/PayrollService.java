package com.workflow.service;

import com.workflow.dto.PayrollBatchRequest;
import com.workflow.dto.PayrollCalculateRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollService {
    private final PayrollRecordRepository payrollRecordRepository;
    private final DailyLogRepository dailyLogRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryAdvanceRepository salaryAdvanceRepository;
    private final MonthlyLockoutRepository monthlyLockoutRepository;
    private final AuditLogService auditLogService;

    public PayrollService(PayrollRecordRepository prr, DailyLogRepository dlr, EmployeeRepository er,
                          SalaryAdvanceRepository sar, MonthlyLockoutRepository mlr, AuditLogService als) {
        this.payrollRecordRepository = prr; this.dailyLogRepository = dlr; this.employeeRepository = er;
        this.salaryAdvanceRepository = sar; this.monthlyLockoutRepository = mlr; this.auditLogService = als;
    }

    @Transactional
    public PayrollRecord calculatePayroll(PayrollCalculateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LocalDate periodStart = LocalDate.parse(request.getPeriodStart());
        LocalDate periodEnd = LocalDate.parse(request.getPeriodEnd());
        List<DailyLog> logs = dailyLogRepository.findByEmployeeAndLogDateBetween(employee, periodStart, periodEnd)
                .stream().filter(l -> l.getStatus() == LogStatus.APPROVED).collect(Collectors.toList());
        int daysWorked = (int) logs.stream().map(DailyLog::getLogDate).distinct().count();
        BigDecimal totalHours = logs.stream().map(l -> l.getHoursWorked() != null ? l.getHoursWorked() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal wageRateAtTime;
        BigDecimal grossPay;
        if (employee.getWageType() == WageType.HOURLY) {
            wageRateAtTime = employee.getHourlyWage() != null ? employee.getHourlyWage() : BigDecimal.ZERO;
            grossPay = wageRateAtTime.multiply(totalHours);
        } else {
            wageRateAtTime = employee.getDailyRate() != null ? employee.getDailyRate() : BigDecimal.ZERO;
            grossPay = wageRateAtTime.multiply(BigDecimal.valueOf(daysWorked));
        }
        String periodLabel = periodStart.getMonthValue() + "/" + periodStart.getYear();
        List<PayrollRecord> existing = payrollRecordRepository.findByEmployeeIdOrderByPeriodStartDesc(employee.getId());
        PayrollRecord record = existing.stream()
                .filter(r -> r.getPeriodLabel().equals(periodLabel)
                        && (r.getStatus() == PayrollStatus.DRAFT
                                || r.getStatus() == PayrollStatus.CALCULATED))
                .findFirst().orElseGet(PayrollRecord::new);
        if (record.getId() == null) record.setEmployee(employee);
        record.setPeriodLabel(periodLabel);
        record.setPeriodStart(periodStart);
        record.setPeriodEnd(periodEnd);
        record.setDaysWorked(daysWorked);
        record.setTotalHours(totalHours);
        record.setWageRateAtTime(wageRateAtTime);
        record.setGrossPay(grossPay);
        record.setNetPay(grossPay);
        record.setStatus(PayrollStatus.CALCULATED);
        PayrollRecord saved = payrollRecordRepository.save(record);
        auditLogService.log("PayrollRecord", saved.getId(), "CALCULATED", null, "CALCULATED", employee.getEmail());
        return saved;
    }

    public List<PayrollRecord> getPayrollRecords(Long employeeId, String periodLabel) {
        List<PayrollRecord> records;
        if (employeeId != null) {
            records = payrollRecordRepository.findByEmployeeIdOrderByPeriodStartDesc(employeeId);
        } else {
            records = payrollRecordRepository.findAll(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "periodStart"));
        }
        if (periodLabel != null)
            records = records.stream().filter(r -> periodLabel.equals(r.getPeriodLabel())).collect(Collectors.toList());
        return records;
    }

    public PayrollRecord getPayrollRecord(Long id) {
        return payrollRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));
    }

    @Transactional
    public PayrollRecord approvePayroll(Long id, Long approverId) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));
        if (record.getStatus() != PayrollStatus.CALCULATED)
            throw new RuntimeException("Payroll record must be in CALCULATED status");
        record.setStatus(PayrollStatus.APPROVED);
        PayrollRecord saved = payrollRecordRepository.save(record);
        auditLogService.log("PayrollRecord", id, "APPROVED", "CALCULATED", "APPROVED", String.valueOf(approverId));
        return saved;
    }

    @Transactional
    public PayrollRecord markAsPaid(Long id, Long paidById) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));
        if (record.getStatus() != PayrollStatus.APPROVED)
            throw new RuntimeException("Payroll record must be in APPROVED status");
        Employee paidBy = employeeRepository.findById(paidById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String yearMonth = record.getPeriodLabel().replace("/", "-");
        if (yearMonth.length() == 4) yearMonth = "0" + yearMonth;
        monthlyLockoutRepository.findByYearMonth(yearMonth).ifPresent(ml -> {
            if (ml.getIsUnlocked() != null && !ml.getIsUnlocked())
                throw new RuntimeException("Month " + yearMonth + " is locked. Unlock it before marking as paid.");
        });

        List<SalaryAdvance> unsettled = salaryAdvanceRepository
                .findByEmployeeIdAndIsSettledFalseAndStatusIn(record.getEmployee().getId(),
                        List.of(AdvanceStatus.DISBURSED, AdvanceStatus.APPROVED));
        BigDecimal totalAdvanceDeduction = unsettled.stream()
                .map(SalaryAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netAfterAdvances = record.getGrossPay().subtract(totalAdvanceDeduction);
        if (netAfterAdvances.compareTo(BigDecimal.ZERO) < 0) netAfterAdvances = BigDecimal.ZERO;
        record.setNetPay(netAfterAdvances);
        record.setDeductions(totalAdvanceDeduction);

        record.setStatus(PayrollStatus.PAID);
        record.setPaidAt(LocalDateTime.now());
        record.setPaidBy(paidBy);
        PayrollRecord saved = payrollRecordRepository.save(record);
        auditLogService.log("PayrollRecord", id, "PAID", "APPROVED", "PAID", paidBy.getEmail());

        for (SalaryAdvance adv : unsettled) {
            adv.setIsSettled(true);
            adv.setSettledInPayrollId(saved.getId());
            salaryAdvanceRepository.save(adv);
            auditLogService.log("SalaryAdvance", adv.getId(), "SETTLED", "DISBURSED", "SETTLED",
                    "Payroll #" + saved.getId());
        }

        return saved;
    }

    @Transactional
    public List<PayrollRecord> batchCalculate(PayrollBatchRequest request) {
        LocalDate periodStart = LocalDate.parse(request.getPeriodStart());
        LocalDate periodEnd = LocalDate.parse(request.getPeriodEnd());
        String yearMonth = periodStart.getMonthValue() + "-" + periodStart.getYear();
        if (yearMonth.length() == 4) yearMonth = "0" + yearMonth;
        monthlyLockoutRepository.findByYearMonth(yearMonth).ifPresent(ml -> {
            if (ml.getIsUnlocked() != null && !ml.getIsUnlocked())
                throw new RuntimeException("Month " + yearMonth + " is locked. Unlock it before calculating payroll.");
        });
        List<Employee> employees;
        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            employees = request.getEmployeeIds().stream()
                    .map(eid -> employeeRepository.findById(eid).orElse(null))
                    .filter(e -> e != null)
                    .collect(Collectors.toList());
        } else {
            employees = employeeRepository.findAll().stream()
                    .filter(Employee::getIsActive).collect(Collectors.toList());
        }
        List<PayrollRecord> results = new ArrayList<>();
        for (Employee employee : employees) {
            PayrollCalculateRequest calcReq = new PayrollCalculateRequest();
            calcReq.setEmployeeId(employee.getId());
            calcReq.setPeriodStart(request.getPeriodStart());
            calcReq.setPeriodEnd(request.getPeriodEnd());
            results.add(calculatePayroll(calcReq));
        }
        return results;
    }
}
