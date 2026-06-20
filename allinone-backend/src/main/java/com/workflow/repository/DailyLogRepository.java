package com.workflow.repository;

import com.workflow.entity.DailyLog;
import com.workflow.entity.Employee;
import com.workflow.entity.LogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.deletedAt IS NULL ORDER BY d.submittedAt DESC")
    List<DailyLog> findAllWithEager();

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.deletedAt IS NULL ORDER BY d.submittedAt DESC")
    Page<DailyLog> findAllWithEager(Pageable pageable);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy " +
           "WHERE d.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR d.employee.id = :employeeId) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND (:date IS NULL OR d.logDate = :date) " +
           "ORDER BY d.submittedAt DESC")
    Page<DailyLog> findFiltered(@Param("employeeId") Long employeeId,
                                @Param("status") LogStatus status,
                                @Param("date") LocalDate date,
                                Pageable pageable);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.employee.id = :employeeId AND d.deletedAt IS NULL ORDER BY d.logDate DESC")
    List<DailyLog> findByEmployeeIdWithEager(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.id IN :ids AND d.deletedAt IS NULL")
    List<DailyLog> findByIdInWithEager(@Param("ids") List<Long> ids);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.employee.id = :employeeId AND d.deletedAt IS NULL ORDER BY d.logDate DESC")
    List<DailyLog> findByEmployeeIdOrderByLogDateDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.id IN :ids AND d.deletedAt IS NULL")
    List<DailyLog> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.status = :status AND d.deletedAt IS NULL ORDER BY d.submittedAt ASC")
    List<DailyLog> findByStatusOrderBySubmittedAtAsc(@Param("status") LogStatus status);

    @Query("SELECT d FROM DailyLog d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.reviewedBy WHERE d.employee = :employee AND d.logDate BETWEEN :start AND :end AND d.deletedAt IS NULL")
    List<DailyLog> findByEmployeeAndLogDateBetween(@Param("employee") Employee employee, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT d FROM DailyLog d WHERE d.logDate BETWEEN :start AND :end AND d.deletedAt IS NULL")
    List<DailyLog> findByLogDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
    @Query("SELECT d FROM DailyLog d WHERE d.logDate BETWEEN :start AND :end AND d.status = :status AND d.deletedAt IS NULL")
    List<DailyLog> findByLogDateBetweenAndStatus(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("status") LogStatus status);
    @Query("SELECT COUNT(d) FROM DailyLog d WHERE d.employee = :employee AND d.logDate BETWEEN :start AND :end AND d.status = :status AND d.deletedAt IS NULL")
    long countByEmployeeAndLogDateBetweenAndStatus(@Param("employee") Employee employee, @Param("start") LocalDate start, @Param("end") LocalDate end, @Param("status") LogStatus status);
    @Query("SELECT COUNT(d) FROM DailyLog d WHERE d.status = :status AND d.deletedAt IS NULL")
    long countByStatus(@Param("status") LogStatus status);
    @Query("SELECT COUNT(d) FROM DailyLog d WHERE d.logDate = :logDate AND d.deletedAt IS NULL")
    long countByLogDate(@Param("logDate") LocalDate logDate);
    @Query("SELECT COUNT(d) FROM DailyLog d WHERE d.employee.id = :employeeId AND d.logDate = :logDate AND d.deletedAt IS NULL")
    long countByEmployeeIdAndLogDate(@Param("employeeId") Long employeeId, @Param("logDate") LocalDate logDate);
    @Query("SELECT COUNT(d) FROM DailyLog d WHERE d.employee.id = :employeeId AND d.status = :status AND d.deletedAt IS NULL")
    long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") LogStatus status);

    @Query("SELECT d FROM DailyLog d WHERE d.employee.id = :employeeId AND d.logDate BETWEEN :start AND :end AND d.deletedAt IS NULL")
    List<DailyLog> findByEmployeeIdAndLogDateBetween(@Param("employeeId") Long employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Modifying
    @Query(value = "UPDATE daily_logs SET employee_id = :targetId WHERE employee_id = :sourceId", nativeQuery = true)
    int transferEmployeeId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE daily_logs SET reviewed_by = :targetId WHERE reviewed_by = :sourceId", nativeQuery = true)
    int transferReviewedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
