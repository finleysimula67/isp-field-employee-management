package com.workflow.controller;

import com.workflow.entity.Employee;
import com.workflow.entity.RecycleBin;
import com.workflow.service.RecycleBinService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/recycle-bin")
public class RecycleBinController {
    private final RecycleBinService recycleBinService;

    public RecycleBinController(RecycleBinService rbs) { this.recycleBinService = rbs; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Page<RecycleBin>> getAll(
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(recycleBinService.getAll(entityType, page, size));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Map<String, Long>> getCount(
            @RequestParam(required = false) String entityType) {
        return ResponseEntity.ok(Map.of("count", recycleBinService.count(entityType)));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<?> restore(@PathVariable Long id,
                                     @AuthenticationPrincipal Employee actor) {
        recycleBinService.restore(id, actor);
        return ResponseEntity.ok(Map.of("message", "Record restored successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> permanentDelete(@PathVariable Long id,
                                             @AuthenticationPrincipal Employee actor) {
        recycleBinService.permanentDelete(id, actor);
        return ResponseEntity.ok(Map.of("message", "Record permanently deleted"));
    }
}
