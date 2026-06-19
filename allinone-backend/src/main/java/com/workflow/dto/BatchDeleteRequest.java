package com.workflow.dto;

import java.util.List;

public class BatchDeleteRequest {
    private List<Long> ids;
    private String fromDate;
    private String toDate;
    private Boolean deleteAll;

    public BatchDeleteRequest() {}

    public List<Long> getIds() { return ids; } public void setIds(List<Long> ids) { this.ids = ids; }
    public String getFromDate() { return fromDate; } public void setFromDate(String fromDate) { this.fromDate = fromDate; }
    public String getToDate() { return toDate; } public void setToDate(String toDate) { this.toDate = toDate; }
    public Boolean getDeleteAll() { return deleteAll; } public void setDeleteAll(Boolean deleteAll) { this.deleteAll = deleteAll; }
}
