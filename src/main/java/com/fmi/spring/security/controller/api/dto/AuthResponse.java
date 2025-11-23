package com.fmi.spring.security.controller.api.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        String username,
        String role,
        Instant expiresAt
) {}