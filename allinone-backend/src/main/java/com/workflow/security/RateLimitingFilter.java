package com.workflow.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    @Value("${app.rate-limit.max-requests:10}")
    private int MAX_REQUESTS;
    @Value("${app.rate-limit.window-ms:60000}")
    private long WINDOW_MS;
    private final Map<String, Window> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();

        if (path.startsWith("/api/")) {
            String ip = getClientIp(req);
            Window window = attempts.compute(ip, (k, v) -> {
                long now = System.currentTimeMillis();
                if (v == null || now - v.start > WINDOW_MS) return new Window(now);
                v.count.incrementAndGet();
                return v;
            });

            if (window.count.get() > MAX_REQUESTS) {
                log.warn("Rate limit exceeded for IP: {}", ip);
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.setStatus(429);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    private static class Window {
        final long start;
        final AtomicInteger count = new AtomicInteger(1);
        Window(long start) { this.start = start; }
    }
}