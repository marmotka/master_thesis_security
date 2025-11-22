package com.fmi.spring.security.exception;

import lombok.Getter;

@Getter
public class DuplicateUserException extends Exception {

    String field;

    public DuplicateUserException(String message, String field) {
        super(message);
        this.field = field;
    }
}
