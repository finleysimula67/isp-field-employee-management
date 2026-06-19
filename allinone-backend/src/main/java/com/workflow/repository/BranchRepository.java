package com.workflow.repository;

import com.workflow.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.manager WHERE b.deletedAt IS NULL")
    List<Branch> findAllWithManager();

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.manager WHERE b.id = :id AND b.deletedAt IS NULL")
    java.util.Optional<Branch> findByIdWithEager(Long id);
}
