package com.workflow.service;

import com.workflow.dto.AttendanceResponse;
import com.workflow.dto.AttendanceResponse.AttendanceStats;
import com.workflow.dto.WageSummaryResponse;
import com.workflow.entity.*;
import com.workflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final EmployeeRepository employeeRepository;
    private final DailyLogRepository dailyLogRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final HolidayRepository holidayRepository;

    public AttendanceService(EmployeeRepository er, DailyLogRepository dlr,
                             LeaveRequestRepository lrr, HolidayRepository hr) {
        this.employeeRepository = er; this.dailyLogRepository = dlr;
        this.leaveRequestRepository = lrr; this.holidayRepository = hr;
    }

    public List<AttendanceResponse> getMonthlyAttendance(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Employee> employees = employeeRepository.findAll();

        List<DailyLog> logs = dailyLogRepository.findByLogDateBetween(start, end);
        List<LeaveRequest> leaves = leaveRequestRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(end, start, LeaveStatus.APPROVED);
        Set<LocalDate> holidays = new HashSet<>(holidayRepository.findByDateBetween(start, end).stream()
                .map(Holiday::getDate).collect(Collectors.toSet()));
        for (Holiday h : holidayRepository.findAll()) {
            if (Boolean.TRUE.equals(h.getIsRecurringYearly())
                    && !holidays.contains(h.getDate())) {
                LocalDate adjusted = h.getDate().withYear(year);
                if (!adjusted.isBefore(start) && !adjusted.isAfter(end))
                    holidays.add(adjusted);
            }
        }

        Map<Long, Map<LocalDate, DailyLog>> logMap = new HashMap<>();
        for (DailyLog log : logs) {
            logMap.computeIfAbsent(log.getEmployee().getId(), k -> new HashMap<>())
                    .put(log.getLogDate(), log);
        }

        Map<Long, Set<LocalDate>> leaveDateMap = new HashMap<>();
        for (LeaveRequest lr : leaves) {
            LocalDate lStart = lr.getStartDate();
            LocalDate lEnd = lr.getEndDate();
            for (LocalDate d = lStart; !d.isAfter(lEnd); d = d.plusDays(1)) {
                if (!d.isBefore(start) && !d.isAfter(end)) {
                    leaveDateMap.computeIfAbsent(lr.getEmployee().getId(), k -> new HashSet<>()).add(d);
                }
            }
        }

        List<AttendanceResponse> result = new ArrayList<>();
        for (Employee emp : employees) {
            Map<String, String> days = new LinkedHashMap<>();
            AttendanceStats stats = new AttendanceStats();
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.toString();
                String status;
                if (holidays.contains(date)) {
                    status = "HOLIDAY";
                    stats.setHoliday(stats.getHoliday() + 1);
                } else if (leaveDateMap.getOrDefault(emp.getId(), Collections.emptySet()).contains(date)) {
                    status = "ON_LEAVE";
                    stats.setOnLeave(stats.getOnLeave() + 1);
                } else {
                    DailyLog log = logMap.getOrDefault(emp.getId(), Collections.emptyMap()).get(date);
                    if (log == null) {
                        status = "ABSENT";
                        stats.setAbsent(stats.getAbsent() + 1);
                    } else if (log.getStatus() == LogStatus.APPROVED) {
                        status = "PRESENT";
                        stats.setPresent(stats.getPresent() + 1);
                    } else {
                        status = "PENDING";
                        stats.setPending(stats.getPending() + 1);
                    }
                }
                days.put(dateStr, status);
            }
            AttendanceResponse ar = new AttendanceResponse();
            ar.setEmployeeId(emp.getId());
            ar.setEmployeeName(emp.getName());
            ar.setDays(days);
            ar.setStats(stats);
            result.add(ar);
        }
        return result;
    }

    public AttendanceResponse getMyMonthlyAttendance(Long employeeId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<DailyLog> logs = dailyLogRepository.findByEmployeeIdAndLogDateBetween(employeeId, start, end);
        List<LeaveRequest> leaves = leaveRequestRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(end, start, LeaveStatus.APPROVED)
                .stream().filter(lr -> lr.getEmployee().getId().equals(employeeId))
                .collect(Collectors.toList());
        Set<LocalDate> holidays = new HashSet<>(holidayRepository.findByDateBetween(start, end).stream()
                .map(Holiday::getDate).collect(Collectors.toSet()));
        for (Holiday h : holidayRepository.findAll()) {
            if (Boolean.TRUE.equals(h.getIsRecurringYearly())
                    && !holidays.contains(h.getDate())) {
                LocalDate adjusted = h.getDate().withYear(year);
                if (!adjusted.isBefore(start) && !adjusted.isAfter(end))
                    holidays.add(adjusted);
            }
        }

        Map<LocalDate, DailyLog> logMap = logs.stream()
                .collect(Collectors.toMap(DailyLog::getLogDate, l -> l, (a, b) -> a));

        Set<LocalDate> leaveDates = new HashSet<>();
        for (LeaveRequest lr : leaves) {
            for (LocalDate d = lr.getStartDate(); !d.isAfter(lr.getEndDate()); d = d.plusDays(1)) {
                if (!d.isBefore(start) && !d.isAfter(end)) leaveDates.add(d);
            }
        }

        Map<String, String> days = new LinkedHashMap<>();
        AttendanceStats stats = new AttendanceStats();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.toString();
            String status;
            if (holidays.contains(date)) {
                status = "HOLIDAY";
                stats.setHoliday(stats.getHoliday() + 1);
            } else if (leaveDates.contains(date)) {
                status = "ON_LEAVE";
                stats.setOnLeave(stats.getOnLeave() + 1);
            } else {
                DailyLog log = logMap.get(date);
                if (log == null) {
                    status = "ABSENT";
                    stats.setAbsent(stats.getAbsent() + 1);
                } else if (log.getStatus() == LogStatus.APPROVED) {
                    status = "PRESENT";
                    stats.setPresent(stats.getPresent() + 1);
                } else {
                    status = "PENDING";
                    stats.setPending(stats.getPending() + 1);
                }
            }
            days.put(dateStr, status);
        }

        AttendanceResponse ar = new AttendanceResponse();
        ar.setEmployeeId(employeeId);
        ar.setEmployeeName(emp.getName());
        ar.setDays(days);
        ar.setStats(stats);
        return ar;
    }

    public List<WageSummaryResponse> getWageSummary(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Employee> employees = employeeRepository.findAll();

        Map<Long, Long> approvedCounts = dailyLogRepository
                .findByLogDateBetweenAndStatus(start, end, LogStatus.APPROVED).stream()
                .collect(Collectors.groupingBy(
                        dl -> dl.getEmployee().getId(),
                        Collectors.mapping(DailyLog::getLogDate, Collectors.toSet())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));

        Set<LocalDate> holidays = holidayRepository.findByDateBetween(start, end).stream()
                .map(Holiday::getDate).collect(Collectors.toSet());

        long totalDays = yearMonth.lengthOfMonth() - holidays.size();

        List<LeaveRequest> leaves = leaveRequestRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(end, start, LeaveStatus.APPROVED);
        Map<Long, Set<LocalDate>> employeeLeaveDates = new HashMap<>();
        for (LeaveRequest lr : leaves) {
            for (LocalDate d = lr.getStartDate(); !d.isAfter(lr.getEndDate()); d = d.plusDays(1)) {
                if (!d.isBefore(start) && !d.isAfter(end))
                    employeeLeaveDates.computeIfAbsent(lr.getEmployee().getId(), k -> new HashSet<>()).add(d);
            }
        }

        List<WageSummaryResponse> result = new ArrayList<>();
        for (Employee emp : employees) {
            long present = approvedCounts.getOrDefault(emp.getId(), 0L);
            long onLeave = employeeLeaveDates.getOrDefault(emp.getId(), Collections.emptySet()).size();
            long absent = totalDays - present - onLeave;
            BigDecimal rate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
            BigDecimal earned = rate.multiply(BigDecimal.valueOf(present));

            WageSummaryResponse ws = new WageSummaryResponse();
            ws.setEmployeeId(emp.getId());
            ws.setEmployeeName(emp.getName());
            ws.setPresentDays(present);
            ws.setAbsentDays(Math.max(0, absent));
            ws.setDailyRate(rate);
            ws.setTotalEarned(earned);
            result.add(ws);
        }
        return result;
    }

    public WageSummaryResponse getMyWageSummary(Long employeeId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        long present = dailyLogRepository
                .findByEmployeeIdAndLogDateBetween(employeeId, start, end).stream()
                .filter(l -> l.getStatus() == LogStatus.APPROVED)
                .map(DailyLog::getLogDate)
                .distinct()
                .count();

        Set<LocalDate> holidays = new HashSet<>(holidayRepository.findByDateBetween(start, end).stream()
                .map(Holiday::getDate).collect(Collectors.toSet()));
        for (Holiday h : holidayRepository.findAll()) {
            if (Boolean.TRUE.equals(h.getIsRecurringYearly())
                    && !holidays.contains(h.getDate())) {
                LocalDate adjusted = h.getDate().withYear(year);
                if (!adjusted.isBefore(start) && !adjusted.isAfter(end))
                    holidays.add(adjusted);
            }
        }
        long totalDays = yearMonth.lengthOfMonth() - holidays.size();
        List<LeaveRequest> leaves = leaveRequestRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(end, start, LeaveStatus.APPROVED);
        long onLeave = leaves.stream()
                .filter(lr -> lr.getEmployee().getId().equals(employeeId))
                .mapToLong(lr -> {
                    LocalDate lStart = lr.getStartDate();
                    LocalDate lEnd = lr.getEndDate();
                    long days = 0;
                    for (LocalDate d = lStart; !d.isAfter(lEnd) && !d.isAfter(end); d = d.plusDays(1))
                        if (!d.isBefore(start) && !holidays.contains(d)) days++;
                    return days;
                }).sum();
        long absent = totalDays - present - onLeave;

        BigDecimal rate = emp.getDailyRate() != null ? emp.getDailyRate() : BigDecimal.valueOf(800);
        BigDecimal earned = rate.multiply(BigDecimal.valueOf(present));

        WageSummaryResponse ws = new WageSummaryResponse();
        ws.setEmployeeId(emp.getId());
        ws.setEmployeeName(emp.getName());
        ws.setPresentDays(present);
        ws.setAbsentDays(Math.max(0, absent));
        ws.setDailyRate(rate);
        ws.setTotalEarned(earned);
        return ws;
    }
}
