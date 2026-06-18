package com.workflow.repository;

import com.workflow.entity.CashCollection;
import com.workflow.entity.CollectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CashCollectionRepository extends JpaRepository<CashCollection, Long> {

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy ORDER BY c.submittedAt DESC")
    List<CashCollection> findAllWithEager();

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy ORDER BY c.submittedAt DESC")
    Page<CashCollection> findAllWithEager(Pageable pageable);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.employee.id = :employeeId ORDER BY c.submittedAt DESC")
    List<CashCollection> findByEmployeeIdWithEager(@Param("employeeId") Long employeeId);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.id IN :ids")
    List<CashCollection> findByIdInWithEager(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.submittedAt BETWEEN :start AND :end ORDER BY c.submittedAt DESC")
    List<CashCollection> findBySubmittedAtBetweenWithEager(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByStatus(CollectionStatus status);
}
