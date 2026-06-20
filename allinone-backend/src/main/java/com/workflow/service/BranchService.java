package com.workflow.service;

import com.workflow.dto.BranchResponse;
import com.workflow.entity.Branch;
import com.workflow.entity.Employee;
import com.workflow.repository.BranchRepository;
import com.workflow.repository.EmployeeRepository;
import com.workflow.service.AuditLogService;
import com.workflow.service.RecycleBinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {
    private static final Logger log = LoggerFactory.getLogger(BranchService.class);
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final RecycleBinService recycleBinService;

    public BranchService(BranchRepository br, EmployeeRepository er,
                         AuditLogService als, RecycleBinService rbs) {
        this.branchRepository = br; this.employeeRepository = er;
        this.auditLogService = als; this.recycleBinService = rbs;
    }

    @Transactional
    public BranchResponse createBranch(String name, String code, String address) {
        Branch branch = branchRepository.save(new Branch(name, code, address));
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(Long id, String name, String code, String address, Long managerId) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        if (name != null) branch.setName(name);
        if (code != null) branch.setCode(code);
        if (address != null) branch.setAddress(address);
        if (managerId != null) {
            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            branch.setManager(manager);
        }
        return toResponse(branchRepository.save(branch));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAllWithManager().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranch(Long id) {
        return toResponse(branchRepository.findByIdWithEager(id)
                .orElseThrow(() -> new RuntimeException("Branch not found")));
    }

    @Transactional
    public void softDeleteBranch(Long id, Employee actor) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        branch.setDeletedAt(LocalDateTime.now());
        branch.setDeletedBy(actor.getId());
        branchRepository.save(branch);
        recycleBinService.softDelete(branch, id, "Branch", actor, null, branch.getCreatedAt());
        auditLogService.log("Branch", id, "SOFT_DELETED", branch.getIsActive() != null ? branch.getIsActive().toString() : null, "DELETED", actor.getEmail());
    }

    private BranchResponse toResponse(Branch b) {
        BranchResponse r = new BranchResponse();
        r.setId(b.getId()); r.setName(b.getName()); r.setCode(b.getCode());
        r.setAddress(b.getAddress());
        r.setManagerName(b.getManager() != null ? b.getManager().getName() : null);
        r.setManagerId(b.getManager() != null ? b.getManager().getId() : null);
        r.setIsActive(b.getIsActive()); r.setCreatedAt(b.getCreatedAt());
        return r;
    }
}
