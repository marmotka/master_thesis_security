package com.fmi.quarkus.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class PasswordChangeException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public PasswordChangeException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

}
