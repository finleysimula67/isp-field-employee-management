package com.workflow.repository;

import com.workflow.entity.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long> {
}
