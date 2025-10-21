package com.fmi.spring.security.exception;

public class DuplicateUserException extends Exception {

    public DuplicateUserException(String message) {
        super(message);
    }
}
