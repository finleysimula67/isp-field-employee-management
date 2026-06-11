package com.workflow.security;

import com.workflow.dto.LoginResponse;
import com.workflow.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/callback}")
    private String redirectUri;

    private final AuthService authService;

    public OAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getName();
        try {
            LoginResponse loginRes = authService.loginWithGoogle(email, name, googleId);
            response.sendRedirect(redirectUri + "?token=" + loginRes.getToken()
                + "&userId=" + loginRes.getUserId() + "&role=" + loginRes.getRole()
                + "&name=" + URLEncoder.encode(loginRes.getName(), StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(loginRes.getEmail(), StandardCharsets.UTF_8)
                + "&approved=true");
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "oauth_failed";
            if (errorMsg.contains("pending admin approval")) {
                response.sendRedirect(redirectUri + "?pending=true"
                    + "&name=" + URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8)
                    + "&email=" + URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(redirectUri + "?error="
                    + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
            }
        }
    }
}
