package com.workflow.repository;

import com.workflow.entity.EmailAllowList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmailAllowListRepository extends JpaRepository<EmailAllowList, Long> {
    @Query("SELECT e FROM EmailAllowList e WHERE e.email = :email AND e.deletedAt IS NULL")
    Optional<EmailAllowList> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EmailAllowList e WHERE e.email = :email AND e.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);
}
