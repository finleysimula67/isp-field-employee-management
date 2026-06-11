package com.workflow.repository;

import com.workflow.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByEmployeeIdOrderByPeriodStartDesc(Long employeeId);
}
