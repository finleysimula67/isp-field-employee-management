package com.workflow.repository;

import com.workflow.entity.PayrollRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    @Query("SELECT p FROM PayrollRecord p WHERE p.employee.id = :employeeId AND p.deletedAt IS NULL ORDER BY p.periodStart DESC")
    List<PayrollRecord> findByEmployeeIdOrderByPeriodStartDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT p FROM PayrollRecord p LEFT JOIN FETCH p.employee " +
           "WHERE p.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR p.employee.id = :employeeId) " +
           "AND (:periodLabel IS NULL OR p.periodLabel = :periodLabel) " +
           "ORDER BY p.periodStart DESC")
    Page<PayrollRecord> findFiltered(@Param("employeeId") Long employeeId,
                                     @Param("periodLabel") String periodLabel,
                                     Pageable pageable);

    @Query("SELECT p FROM PayrollRecord p JOIN FETCH p.employee WHERE p.id = :id AND p.deletedAt IS NULL")
    java.util.Optional<PayrollRecord> findByIdWithEmployee(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE payroll_records SET employee_id = :targetId WHERE employee_id = :sourceId", nativeQuery = true)
    int transferEmployeeId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE payroll_records SET paid_by = :targetId WHERE paid_by = :sourceId", nativeQuery = true)
    int transferPaidBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
