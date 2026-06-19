package com.workflow.repository;

import com.workflow.entity.MonthlyLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MonthlyLockoutRepository extends JpaRepository<MonthlyLockout, Long> {
    @Query("SELECT m FROM MonthlyLockout m WHERE m.yearMonth = :yearMonth AND m.deletedAt IS NULL")
    Optional<MonthlyLockout> findByYearMonth(@Param("yearMonth") String yearMonth);
}
