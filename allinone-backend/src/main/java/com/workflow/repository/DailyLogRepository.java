package com.workflow.repository;

import com.workflow.entity.DailyLog;
import com.workflow.entity.Employee;
import com.workflow.entity.LogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    List<DailyLog> findByEmployeeIdOrderByLogDateDesc(Long employeeId);
    List<DailyLog> findByStatusOrderBySubmittedAtAsc(LogStatus status);
    List<DailyLog> findByEmployeeAndLogDateBetween(Employee employee, LocalDate start, LocalDate end);
    List<DailyLog> findByLogDateBetween(LocalDate start, LocalDate end);
    List<DailyLog> findByLogDateBetweenAndStatus(LocalDate start, LocalDate end, LogStatus status);
    long countByEmployeeAndLogDateBetweenAndStatus(Employee employee, LocalDate start, LocalDate end, LogStatus status);
    long countByStatus(LogStatus status);
    long countByLogDate(LocalDate logDate);
    long countByEmployeeIdAndLogDate(Long employeeId, LocalDate logDate);
    long countByEmployeeIdAndStatus(Long employeeId, LogStatus status);
    List<DailyLog> findByEmployeeIdAndLogDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<DailyLog> findByIdIn(List<Long> ids);
}
