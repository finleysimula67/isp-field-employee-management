package com.workflow.repository;

import com.workflow.entity.MonthlyLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MonthlyLockoutRepository extends JpaRepository<MonthlyLockout, Long> {
    Optional<MonthlyLockout> findByYearMonth(String yearMonth);
}
