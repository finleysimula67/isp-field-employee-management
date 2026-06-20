package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
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

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(@RequestParam("file") MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("Filename is required"));

        String originalNameClean = originalName.replaceAll("[\\.]{2,}|[\\\\/]", "_");
        String ext = "";
        if (originalNameClean.contains("."))
            ext = originalNameClean.substring(originalNameClean.lastIndexOf(".")).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext))
            return ResponseEntity.badRequest().body(ApiResponse.error("File type not allowed: " + ext));

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("Rejected upload {} with content-type: {} (extension: {})", originalName, contentType, ext);
            return ResponseEntity.badRequest().body(ApiResponse.error("File type not allowed"));
        }

        if (file.getSize() > 10_485_760)
            return ResponseEntity.badRequest().body(ApiResponse.error("File too large (max 10MB)"));

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filename = UUID.randomUUID().toString() + ext;
        Path path = Paths.get(uploadDir).resolve(filename).normalize();
        if (!path.startsWith(Paths.get(uploadDir).normalize()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid file path"));

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, path);
            log.info("Uploaded: {} ({} bytes, {})", filename, file.getSize(), path.toAbsolutePath());
            String url = "/uploads/" + filename;
            return ResponseEntity.ok(ApiResponse.ok("File uploaded", Map.of("url", url, "filename", filename)));
        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed"));
        }
    }
}