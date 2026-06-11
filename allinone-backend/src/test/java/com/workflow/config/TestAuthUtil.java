package com.workflow.config;

import com.workflow.entity.Employee;
import com.workflow.entity.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

public class TestAuthUtil {

    public static Employee createEmployee(Long id, String email, String name, Role role) {
        Employee emp = new Employee();
        emp.setId(id);
        emp.setEmail(email);
        emp.setName(name);
        emp.setRole(role);
        emp.setIsActive(true);
        emp.setIsAccountApproved(true);
        return emp;
    }

    public static void setAuth(Employee emp) {
        var auth = new UsernamePasswordAuthenticationToken(emp, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + emp.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static RequestPostProcessor withAuth(Employee emp) {
        return request -> {
            setAuth(emp);
            return request;
        };
    }
}
