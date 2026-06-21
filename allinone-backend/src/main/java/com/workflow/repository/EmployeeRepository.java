package com.workflow.repository;

import com.workflow.entity.Employee;
import com.workflow.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByIsOwnerTrue();
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByAuthProviderId(String authProviderId);
    Optional<Employee> findByResetToken(String resetToken);
    boolean existsByEmail(String email);
    List<Employee> findByRoleIn(List<Role> roles);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.branch")
    List<Employee> findAllWithBranch();

    @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.branch",
           countQuery = "SELECT COUNT(e) FROM Employee e")
    Page<Employee> findAllWithBranch(Pageable pageable);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.branch WHERE e.id = :id")
    Optional<Employee> findByIdWithBranch(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Employee e SET e.isActive = false, e.isOwner = false WHERE e.id = :id")
    int deactivateAndRemoveOwnership(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Employee e SET e.tokenVersion = COALESCE(e.tokenVersion, 0) + 1 WHERE e.id = :id")
    int incrementTokenVersion(@Param("id") Long id);
}
