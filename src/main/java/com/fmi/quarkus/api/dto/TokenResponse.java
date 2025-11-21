package com.fmi.quarkus.api.dto;

import java.util.Set;

public class TokenResponse {

    public String token;
    private static final String TOKEN_TYPE = "Bearer";
    public long expiresAt;        // epoch seconds
    public String subject;
    public Set<String> roles;

    public TokenResponse() {
    }

    public TokenResponse(String token, long expiresAt, String subject, Set<String> roles) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.subject = subject;
        this.roles = roles;
    }
}
