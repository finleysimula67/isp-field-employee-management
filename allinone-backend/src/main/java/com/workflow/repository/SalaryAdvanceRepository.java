package com.workflow.repository;

import com.workflow.entity.AdvanceStatus;
import com.workflow.entity.SalaryAdvance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, Long> {
    @Query("SELECT s FROM SalaryAdvance s LEFT JOIN FETCH s.employee LEFT JOIN FETCH s.approvedBy WHERE s.deletedAt IS NULL ORDER BY s.requestDate DESC")
    List<SalaryAdvance> findAllWithEager();

    @Query(value = "SELECT s FROM SalaryAdvance s LEFT JOIN FETCH s.employee LEFT JOIN FETCH s.approvedBy WHERE s.deletedAt IS NULL",
           countQuery = "SELECT COUNT(s) FROM SalaryAdvance s WHERE s.deletedAt IS NULL")
    Page<SalaryAdvance> findAllWithEager(Pageable pageable);

    @Query("SELECT s FROM SalaryAdvance s LEFT JOIN FETCH s.employee LEFT JOIN FETCH s.approvedBy WHERE s.employee.id = :employeeId AND s.deletedAt IS NULL ORDER BY s.requestDate DESC")
    List<SalaryAdvance> findByEmployeeIdOrderByRequestDateDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM SalaryAdvance s LEFT JOIN FETCH s.employee LEFT JOIN FETCH s.approvedBy WHERE s.employee.id = :employeeId AND s.isSettled = false AND s.status IN :statuses AND s.deletedAt IS NULL")
    List<SalaryAdvance> findByEmployeeIdAndIsSettledFalseAndStatusIn(@Param("employeeId") Long employeeId, @Param("statuses") List<AdvanceStatus> statuses);

    @Query("SELECT s FROM SalaryAdvance s LEFT JOIN FETCH s.employee LEFT JOIN FETCH s.approvedBy " +
           "WHERE s.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR s.employee.id = :employeeId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "ORDER BY s.requestDate DESC")
    Page<SalaryAdvance> findFiltered(@Param("employeeId") Long employeeId,
                                     @Param("status") AdvanceStatus status,
                                     Pageable pageable);

    @Query("SELECT s FROM SalaryAdvance s JOIN FETCH s.employee WHERE s.id = :id AND s.deletedAt IS NULL")
    java.util.Optional<SalaryAdvance> findByIdWithEmployee(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE salary_advances SET employee_id = :targetId WHERE employee_id = :sourceId", nativeQuery = true)
    int transferEmployeeId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE salary_advances SET approved_by = :targetId WHERE approved_by = :sourceId", nativeQuery = true)
    int transferApprovedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
