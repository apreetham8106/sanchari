package com.sanchari.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends SanchariException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
