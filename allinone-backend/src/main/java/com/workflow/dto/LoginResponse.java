package com.workflow.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String name;
    private String role;
    private Long userId;
    private String message;
    private boolean requiresMfa;

    public LoginResponse() {}
    public LoginResponse(String token, String email, String name, String role, Long userId, String message) {
        this.token = token; this.email = email; this.name = name;
        this.role = role; this.userId = userId; this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRequiresMfa() { return requiresMfa; }
    public void setRequiresMfa(boolean requiresMfa) { this.requiresMfa = requiresMfa; }

    public static LoginResponseBuilder builder() { return new LoginResponseBuilder(); }
    public static class LoginResponseBuilder {
        private String token, email, name, role, message; private Long userId; private boolean requiresMfa;
        LoginResponseBuilder() {}
        public LoginResponseBuilder token(String token) { this.token = token; return this; }
        public LoginResponseBuilder email(String email) { this.email = email; return this; }
        public LoginResponseBuilder name(String name) { this.name = name; return this; }
        public LoginResponseBuilder role(String role) { this.role = role; return this; }
        public LoginResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public LoginResponseBuilder message(String message) { this.message = message; return this; }
        public LoginResponseBuilder requiresMfa(boolean requiresMfa) { this.requiresMfa = requiresMfa; return this; }
        public LoginResponse build() { var r = new LoginResponse(token, email, name, role, userId, message); r.setRequiresMfa(requiresMfa); return r; }
    }
}
