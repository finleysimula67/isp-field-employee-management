package com.workflow.dto;

import java.util.Map;

public class PurgeResult {
    private Map<String, Integer> deletedCounts;
    private int totalDeleted;

    public PurgeResult() {}
    public PurgeResult(Map<String, Integer> deletedCounts, int totalDeleted) {
        this.deletedCounts = deletedCounts; this.totalDeleted = totalDeleted;
    }

    public Map<String, Integer> getDeletedCounts() { return deletedCounts; }
    public void setDeletedCounts(Map<String, Integer> deletedCounts) { this.deletedCounts = deletedCounts; }
    public int getTotalDeleted() { return totalDeleted; }
    public void setTotalDeleted(int totalDeleted) { this.totalDeleted = totalDeleted; }
}
