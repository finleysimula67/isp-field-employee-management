package com.workflow.repository;

import com.workflow.entity.EmailAllowList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailAllowListRepository extends JpaRepository<EmailAllowList, Long> {
    Optional<EmailAllowList> findByEmail(String email);
    boolean existsByEmail(String email);
}
