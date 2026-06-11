package com.workflow.controller;

import com.workflow.dto.ApiResponse;
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

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".csv"
    );

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(@RequestParam("file") MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("Filename is required"));

        String ext = "";
        if (originalName.contains("."))
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext))
            return ResponseEntity.badRequest().body(ApiResponse.error("File type not allowed: " + ext));

        if (file.getSize() > 10_485_760)
            return ResponseEntity.badRequest().body(ApiResponse.error("File too large (max 10MB)"));

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filename = UUID.randomUUID().toString() + ext;
        Path path = Paths.get(uploadDir, filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, path);
            String url = "/uploads/" + filename;
            return ResponseEntity.ok(ApiResponse.ok("File uploaded", Map.of("url", url, "filename", filename)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
