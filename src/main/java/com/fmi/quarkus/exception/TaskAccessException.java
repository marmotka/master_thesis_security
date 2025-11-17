package com.fmi.quarkus.exception;

public class TaskAccessException extends RuntimeException {
    public TaskAccessException(String message) {
        super(message);
    }
}
