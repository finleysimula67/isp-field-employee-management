package com.workflow.websocket;

import java.security.Principal;

public class StompPrincipal implements Principal {
    private final String name;
    private final Long employeeId;

    public StompPrincipal(String name, Long employeeId) {
        this.name = name;
        this.employeeId = employeeId;
    }

    public Long getEmployeeId() { return employeeId; }
    @Override public String getName() { return name; }
}
