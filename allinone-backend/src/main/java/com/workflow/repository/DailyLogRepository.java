package com.workflow.repository;

import com.workflow.entity.DailyLog;
import com.workflow.entity.Employee;
import com.workflow.entity.LogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy ORDER BY d.submittedAt DESC")
    List<DailyLog> findAllWithEager();

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy ORDER BY d.submittedAt DESC")
    Page<DailyLog> findAllWithEager(Pageable pageable);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.employee.id = :employeeId ORDER BY d.logDate DESC")
    List<DailyLog> findByEmployeeIdWithEager(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.id IN :ids")
    List<DailyLog> findByIdInWithEager(@Param("ids") List<Long> ids);

    List<DailyLog> findByEmployeeIdOrderByLogDateDesc(Long employeeId);
    List<DailyLog> findByIdIn(List<Long> ids);
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
}
