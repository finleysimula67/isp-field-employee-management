package com.workflow.repository;

import com.workflow.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.manager WHERE b.deletedAt IS NULL")
    List<Branch> findAllWithManager();

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.manager WHERE b.id = :id AND b.deletedAt IS NULL")
    java.util.Optional<Branch> findByIdWithEager(Long id);

    @Modifying
    @Query(value = "UPDATE branches SET manager_id = :targetId WHERE manager_id = :sourceId", nativeQuery = true)
    int transferManagerId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Modifying
    @Query(value = "UPDATE branches SET manager_id = NULL WHERE manager_id = :sourceId", nativeQuery = true)
    int clearManagerId(@Param("sourceId") Long sourceId);
}
