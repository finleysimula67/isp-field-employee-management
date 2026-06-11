package com.workflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.Duration;
import java.time.Instant;

@Service
public class FileCleanupService {
    private static final Logger log = LoggerFactory.getLogger(FileCleanupService.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.cleanup-days:30}")
    private int cleanupDays;

    @Scheduled(cron = "${app.upload.cleanup-cron:0 0 3 * * ?}")
    public void cleanOldFiles() {
        File dir = new File(uploadDir);
        if (!dir.exists() || !dir.isDirectory()) return;

        Instant cutoff = Instant.now().minus(Duration.ofDays(cleanupDays));
        int deleted = 0;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && Instant.ofEpochMilli(file.lastModified()).isBefore(cutoff)) {
                if (file.delete()) {
                    deleted++;
                } else {
                    log.warn("Could not delete old upload: {}", file.getName());
                }
            }
        }

        if (deleted > 0) {
            log.info("Cleaned up {} old upload files from {}", deleted, uploadDir);
        }
    }
}
