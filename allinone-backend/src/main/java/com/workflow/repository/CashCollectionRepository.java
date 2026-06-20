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

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.deletedAt IS NULL ORDER BY c.submittedAt DESC")
    List<CashCollection> findAllWithEager();

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.deletedAt IS NULL ORDER BY c.submittedAt DESC")
    Page<CashCollection> findAllWithEager(Pageable pageable);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.employee.id = :employeeId AND c.deletedAt IS NULL ORDER BY c.submittedAt DESC")
    List<CashCollection> findByEmployeeIdWithEager(@Param("employeeId") Long employeeId);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.id IN :ids AND c.deletedAt IS NULL")
    List<CashCollection> findByIdInWithEager(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy WHERE c.submittedAt BETWEEN :start AND :end AND c.deletedAt IS NULL ORDER BY c.submittedAt DESC")
    List<CashCollection> findBySubmittedAtBetweenWithEager(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c FROM CashCollection c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.reviewedBy " +
           "WHERE c.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR c.employee.id = :employeeId) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "ORDER BY c.submittedAt DESC")
    Page<CashCollection> findFiltered(@Param("employeeId") Long employeeId,
                                      @Param("status") CollectionStatus status,
                                      Pageable pageable);

    @Query("SELECT COUNT(c) FROM CashCollection c WHERE c.status = :status AND c.deletedAt IS NULL")
    long countByStatus(@Param("status") CollectionStatus status);
}
