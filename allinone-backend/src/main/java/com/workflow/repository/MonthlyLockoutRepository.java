package com.workflow.repository;

import com.workflow.entity.MonthlyLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MonthlyLockoutRepository extends JpaRepository<MonthlyLockout, Long> {
    @Query("SELECT m FROM MonthlyLockout m WHERE m.yearMonth = :yearMonth AND m.deletedAt IS NULL")
    Optional<MonthlyLockout> findByYearMonth(@Param("yearMonth") String yearMonth);

    @Modifying
    @Query(value = "UPDATE monthly_lockouts SET locked_by = :targetId WHERE locked_by = :sourceId", nativeQuery = true)
    int transferLockedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE monthly_lockouts SET unlocked_by = :targetId WHERE unlocked_by = :sourceId", nativeQuery = true)
    int transferUnlockedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
