package com.workflow.repository;

import com.workflow.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT h FROM Holiday h WHERE h.date = :date AND h.deletedAt IS NULL")
    Optional<Holiday> findByDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.date BETWEEN :start AND :end AND h.deletedAt IS NULL ORDER BY h.date ASC")
    List<Holiday> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
