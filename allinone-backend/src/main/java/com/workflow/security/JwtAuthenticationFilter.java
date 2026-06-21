package com.workflow.security;

import com.workflow.entity.Employee;
import com.workflow.repository.EmployeeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeRepository employeeRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, EmployeeRepository employeeRepository) {
        this.jwtTokenProvider = jwtTokenProvider; this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            Employee employee = employeeRepository.findById(userId).orElse(null);
            if (employee != null && Boolean.TRUE.equals(employee.getIsActive())
                    && Boolean.TRUE.equals(employee.getIsAccountApproved())) {
                int tokenVersion = jwtTokenProvider.getTokenVersionFromToken(token);
                int dbVersion = employee.getTokenVersion() != null ? employee.getTokenVersion() : 0;
                if (tokenVersion == dbVersion) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(employee, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) return bearer.substring(7);
        return null;
    }
}
