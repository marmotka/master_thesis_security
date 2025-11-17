package com.fmi.quarkus.exception;

import java.util.Collections;
import java.util.Map;

public class TaskValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public TaskValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
