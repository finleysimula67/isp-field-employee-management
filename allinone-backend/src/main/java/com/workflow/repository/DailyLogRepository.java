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

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.deletedAt IS NULL ORDER BY d.submittedAt DESC")
    List<DailyLog> findAllWithEager();

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.deletedAt IS NULL ORDER BY d.submittedAt DESC")
    Page<DailyLog> findAllWithEager(Pageable pageable);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.employee.id = :employeeId AND d.deletedAt IS NULL ORDER BY d.logDate DESC")
    List<DailyLog> findByEmployeeIdWithEager(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.id IN :ids AND d.deletedAt IS NULL")
    List<DailyLog> findByIdInWithEager(@Param("ids") List<Long> ids);

    @Query("SELECT d FROM DailyLog d WHERE d.employee.id = :employeeId AND d.deletedAt IS NULL ORDER BY d.logDate DESC")
    List<DailyLog> findByEmployeeIdOrderByLogDateDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM DailyLog d WHERE d.id IN :ids AND d.deletedAt IS NULL")
    List<DailyLog> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT d FROM DailyLog d WHERE d.status = :status AND d.deletedAt IS NULL ORDER BY d.submittedAt ASC")
    List<DailyLog> findByStatusOrderBySubmittedAtAsc(@Param("status") LogStatus status);

    @Query("SELECT d FROM DailyLog d WHERE d.employee = :employee AND d.logDate BETWEEN :start AND :end AND d.deletedAt IS NULL")
    List<DailyLog> findByEmployeeAndLogDateBetween(@Param("employee") Employee employee, @Param("start") LocalDate start, @Param("end") LocalDate end);

    List<DailyLog> findByLogDateBetween(LocalDate start, LocalDate end);
    List<DailyLog> findByLogDateBetweenAndStatus(LocalDate start, LocalDate end, LogStatus status);
    long countByEmployeeAndLogDateBetweenAndStatus(Employee employee, LocalDate start, LocalDate end, LogStatus status);
    long countByStatus(LogStatus status);
    long countByLogDate(LocalDate logDate);
    long countByEmployeeIdAndLogDate(Long employeeId, LocalDate logDate);
    long countByEmployeeIdAndStatus(Long employeeId, LogStatus status);

    @Query("SELECT d FROM DailyLog d WHERE d.employee.id = :employeeId AND d.logDate BETWEEN :start AND :end AND d.deletedAt IS NULL")
    List<DailyLog> findByEmployeeIdAndLogDateBetween(@Param("employeeId") Long employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
