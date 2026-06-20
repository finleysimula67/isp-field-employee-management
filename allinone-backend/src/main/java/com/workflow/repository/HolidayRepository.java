package com.workflow.repository;

import com.workflow.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT h FROM Holiday h LEFT JOIN FETCH h.createdBy WHERE h.deletedAt IS NULL ORDER BY h.date ASC")
    List<Holiday> findAllWithEager();

    @Query("SELECT h FROM Holiday h LEFT JOIN FETCH h.createdBy WHERE h.id = :id AND h.deletedAt IS NULL")
    Optional<Holiday> findByIdWithEager(@Param("id") Long id);

    @Query("SELECT h FROM Holiday h WHERE h.date = :date AND h.deletedAt IS NULL")
    Optional<Holiday> findByDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h LEFT JOIN FETCH h.createdBy WHERE h.date BETWEEN :start AND :end AND h.deletedAt IS NULL ORDER BY h.date ASC")
    List<Holiday> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Modifying
    @Query(value = "UPDATE holidays SET created_by = :targetId WHERE created_by = :sourceId", nativeQuery = true)
    int transferCreatedBy(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
