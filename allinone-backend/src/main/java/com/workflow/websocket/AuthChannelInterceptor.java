package com.workflow.websocket;

import com.workflow.entity.Employee;
import com.workflow.repository.EmployeeRepository;
import com.workflow.security.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeRepository employeeRepository;

    public AuthChannelInterceptor(JwtTokenProvider jwtTokenProvider, EmployeeRepository employeeRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    Employee employee = employeeRepository.findById(userId).orElse(null);
                    if (employee != null && Boolean.TRUE.equals(employee.getIsActive())
                            && Boolean.TRUE.equals(employee.getIsAccountApproved())) {
                        accessor.setUser(new StompPrincipal(userId.toString(), userId));
                    }
                }
            }
        }
        return message;
    }
}
