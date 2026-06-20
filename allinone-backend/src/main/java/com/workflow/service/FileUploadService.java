package com.workflow.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".csv"
    );
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/csv"
    );

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    void init() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("Created upload directory: {}", dir.getAbsolutePath());
        }
        log.info("Upload directory: {}", dir.getAbsolutePath());
    }

    public String uploadFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank())
            throw new IllegalArgumentException("Filename is required");

        String originalNameClean = originalName.replaceAll("[\\.]{2,}|[\\\\/]", "_");
        String ext = "";
        if (originalNameClean.contains("."))
            ext = originalNameClean.substring(originalNameClean.lastIndexOf(".")).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext))
            throw new IllegalArgumentException("File type not allowed: " + ext);

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("Rejected upload {} with content-type: {} (extension: {})", originalName, contentType, ext);
            throw new IllegalArgumentException("File type not allowed");
        }

        if (file.getSize() > 10_485_760)
            throw new IllegalArgumentException("File too large (max 10MB)");

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filename = UUID.randomUUID().toString() + ext;
        Path path = Paths.get(uploadDir).resolve(filename).normalize();
        if (!path.startsWith(Paths.get(uploadDir).normalize()))
            throw new IllegalArgumentException("Invalid file path");

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, path);
            log.info("Uploaded: {} ({} bytes, {})", filename, file.getSize(), path.toAbsolutePath());
            return filename;
        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            throw new RuntimeException("Upload failed", e);
        }
    }
}
