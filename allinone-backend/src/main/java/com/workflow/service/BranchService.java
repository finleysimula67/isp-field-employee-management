package com.workflow.service;

import com.workflow.dto.BranchResponse;
import com.workflow.entity.Branch;
import com.workflow.entity.Employee;
import com.workflow.repository.BranchRepository;
import com.workflow.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;

    public BranchService(BranchRepository br, EmployeeRepository er) {
        this.branchRepository = br; this.employeeRepository = er;
    }

    public BranchResponse createBranch(String name, String code, String address) {
        Branch branch = branchRepository.save(new Branch(name, code, address));
        return toResponse(branch);
    }

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

    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAllWithManager().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BranchResponse getBranch(Long id) {
        return toResponse(branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found")));
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
