package com.workflow.entity;

import java.time.LocalDateTime;

public interface SoftDeletable {
    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);
    Long getDeletedBy();
    void setDeletedBy(Long deletedBy);
}
