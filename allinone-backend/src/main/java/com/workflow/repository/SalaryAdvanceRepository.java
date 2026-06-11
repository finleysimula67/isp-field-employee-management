package com.workflow.repository;

import com.workflow.entity.AdvanceStatus;
import com.workflow.entity.SalaryAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, Long> {
    List<SalaryAdvance> findByEmployeeIdOrderByRequestDateDesc(Long employeeId);
    List<SalaryAdvance> findByEmployeeIdAndIsSettledFalseAndStatusIn(Long employeeId, List<AdvanceStatus> statuses);
}
