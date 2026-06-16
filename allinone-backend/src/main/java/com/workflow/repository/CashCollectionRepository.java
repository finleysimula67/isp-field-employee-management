package com.workflow.repository;

import com.workflow.entity.CashCollection;
import com.workflow.entity.CollectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CashCollectionRepository extends JpaRepository<CashCollection, Long> {
    List<CashCollection> findByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);
    List<CashCollection> findByIdIn(List<Long> ids);
    long countByStatus(CollectionStatus status);
}
