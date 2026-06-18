package com.workflow.repository;

import com.workflow.entity.Employee;
import com.workflow.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByIsOwnerTrue();
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByAuthProviderId(String authProviderId);
    Optional<Employee> findByResetToken(String resetToken);
    boolean existsByEmail(String email);
    List<Employee> findByRoleIn(List<Role> roles);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.branch")
    List<Employee> findAllWithBranch();
}
