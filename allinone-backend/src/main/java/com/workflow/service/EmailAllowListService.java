package com.workflow.service;

import com.workflow.entity.EmailAllowList;
import com.workflow.entity.Employee;
import com.workflow.repository.EmailAllowListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EmailAllowListService {
    private final EmailAllowListRepository repository;

    public EmailAllowListService(EmailAllowListRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EmailAllowList> findAll() {
        return repository.findAll();
    }

    @Transactional
    public EmailAllowList addEmail(String email, Employee addedBy) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email is required");
        email = email.trim().toLowerCase();
        if (repository.existsByEmail(email))
            throw new RuntimeException("Email already in allow list");
        EmailAllowList entry = new EmailAllowList(email);
        entry.setAddedBy(addedBy);
        return repository.save(entry);
    }

    @Transactional
    public void removeById(Long id) {
        repository.deleteById(id);
    }
}
