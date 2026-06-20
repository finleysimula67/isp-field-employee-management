package com.workflow.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleTokenVerifier {
    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);
    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final String EXPECTED_ISSUER = "https://accounts.google.com";

    private final String clientId;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleTokenVerifier(@Value("${GOOGLE_CLIENT_ID}") String clientId) {
        this.clientId = clientId;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleUser verify(String idToken) {
        try {
            String url = TOKEN_INFO_URL + idToken;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode claims = objectMapper.readTree(response);

            String aud = claims.get("aud").asText();
            String iss = claims.get("iss").asText();
            if (!clientId.equals(aud)) {
                throw new RuntimeException("Token audience mismatch: " + aud);
            }
            if (!EXPECTED_ISSUER.equals(iss)) {
                throw new RuntimeException("Token issuer mismatch: " + iss);
            }

            String email = claims.has("email") ? claims.get("email").asText() : null;
            String name = claims.has("name") ? claims.get("name").asText() : null;
            String sub = claims.get("sub").asText();

            if (email == null) {
                throw new RuntimeException("Email not available from Google token");
            }

            return new GoogleUser(sub, email, name != null ? name : email);
        } catch (Exception e) {
            log.warn("Google token verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Google token: " + e.getMessage());
        }
    }

    public record GoogleUser(String googleId, String email, String name) {}
}
