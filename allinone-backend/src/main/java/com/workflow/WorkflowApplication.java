package com.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkflowApplication {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
