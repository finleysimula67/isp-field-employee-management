package com.workflow.controller;

import com.workflow.dto.ApiResponse;
import com.workflow.dto.TransferRequest;
import com.workflow.dto.TransferResult;
import com.workflow.entity.Employee;
import com.workflow.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TransferResult>> transfer(
            @RequestBody TransferRequest request,
            @AuthenticationPrincipal Employee actor) {
        TransferResult result = transferService.transferAllData(
            request.getSourceEmployeeId(),
            request.getTargetEmployeeId(),
            request.isDeleteSource(),
            actor.getId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Transfer completed", result));
    }
}
