package com.workflow.service;

import com.workflow.entity.Employee;
import com.workflow.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtService(JwtTokenProvider jwtTokenProvider) { this.jwtTokenProvider = jwtTokenProvider; }

    public String generateToken(Employee employee) {
        return jwtTokenProvider.generateToken(employee.getId(), employee.getEmail(), employee.getRole().name());
    }
}
