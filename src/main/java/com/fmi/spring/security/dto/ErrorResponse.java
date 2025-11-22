package com.fmi.spring.security.dto;

public record ErrorResponse(
        String code,
        String message
) {}
