package com.workflow.repository;

import com.workflow.entity.LeaveRequest;
import com.workflow.entity.Employee;
import com.workflow.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);
    List<LeaveRequest> findByStatusOrderBySubmittedAtAsc(LeaveStatus status);
    List<LeaveRequest> findByEmployee(Employee employee);
    long countByStatus(LeaveStatus status);
    List<LeaveRequest> findByIdIn(List<Long> ids);
    List<LeaveRequest> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            LocalDate endDate, LocalDate startDate, LeaveStatus status);
}
