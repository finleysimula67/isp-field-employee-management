package com.workflow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private static String statusText(int status) {
        return switch (status) {
            case 400 -> "Bad request";
            case 401 -> "Unauthorized";
            case 403 -> "Access denied";
            case 404 -> "Not found";
            case 405 -> "Method not allowed";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable entity";
            case 429 -> "Too many requests";
            case 502 -> "Bad gateway";
            case 503 -> "Service unavailable";
            default -> "Internal error";
        };
    }

    @GetMapping("/")
    public Object root() {
        return ResponseEntity.ok(Map.of("success", true, "message", "API is running"));
    }

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, HttpServletResponse response) {
        String path = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        if (path != null && path.startsWith("/api/")) {
            int status = response.getStatus() > 0 ? response.getStatus() : 500;
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", statusText(status), "data", null));
        }

        if (path != null && (path.startsWith("/uploads/") || path.startsWith("/ws") || path.startsWith("/actuator"))) {
            int status = response.getStatus() > 0 ? response.getStatus() : 404;
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", statusText(status), "data", null));
        }

        return "forward:/index.html";
    }
}
