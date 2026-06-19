package com.workflow.repository;

import com.workflow.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    @Query("SELECT p FROM PayrollRecord p WHERE p.employee.id = :employeeId AND p.deletedAt IS NULL ORDER BY p.periodStart DESC")
    List<PayrollRecord> findByEmployeeIdOrderByPeriodStartDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT p FROM PayrollRecord p JOIN FETCH p.employee WHERE p.id = :id AND p.deletedAt IS NULL")
    java.util.Optional<PayrollRecord> findByIdWithEmployee(@Param("id") Long id);
}
