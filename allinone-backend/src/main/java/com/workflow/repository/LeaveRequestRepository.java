package com.workflow.repository;

import com.workflow.entity.LeaveRequest;
import com.workflow.entity.Employee;
import com.workflow.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.deletedAt IS NULL ORDER BY l.submittedAt DESC")
    List<LeaveRequest> findAllWithEager();

    @Query(value = "SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.deletedAt IS NULL",
           countQuery = "SELECT COUNT(l) FROM LeaveRequest l WHERE l.deletedAt IS NULL")
    Page<LeaveRequest> findAllWithEager(Pageable pageable);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.employee.id = :employeeId AND l.deletedAt IS NULL ORDER BY l.submittedAt DESC")
    List<LeaveRequest> findByEmployeeIdOrderBySubmittedAtDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.status = :status AND l.deletedAt IS NULL ORDER BY l.submittedAt ASC")
    List<LeaveRequest> findByStatusOrderBySubmittedAtAsc(@Param("status") LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee = :employee AND l.deletedAt IS NULL")
    List<LeaveRequest> findByEmployee(@Param("employee") Employee employee);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy " +
           "WHERE l.deletedAt IS NULL " +
           "AND (:employeeId IS NULL OR l.employee.id = :employeeId) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "ORDER BY l.submittedAt DESC")
    Page<LeaveRequest> findFiltered(@Param("employeeId") Long employeeId,
                                    @Param("status") LeaveStatus status,
                                    Pageable pageable);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = :status AND l.deletedAt IS NULL")
    long countByStatus(@Param("status") LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.id IN :ids AND l.deletedAt IS NULL")
    List<LeaveRequest> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.startDate <= :endDate AND l.endDate >= :startDate AND l.status = :status AND l.deletedAt IS NULL")
    List<LeaveRequest> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            @Param("endDate") LocalDate endDate, @Param("startDate") LocalDate startDate, @Param("status") LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee LEFT JOIN FETCH l.reviewedBy WHERE l.employee = :employee AND l.startDate <= :endDate AND l.endDate >= :startDate AND l.status IN :statuses AND l.deletedAt IS NULL")
    List<LeaveRequest> findByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
            @Param("employee") Employee employee, @Param("endDate") LocalDate endDate,
            @Param("startDate") LocalDate startDate, @Param("statuses") List<LeaveStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LeaveRequest l WHERE l.id = :id")
    java.util.Optional<LeaveRequest> findByIdWithLock(@Param("id") Long id);
}
