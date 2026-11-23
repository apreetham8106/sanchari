package com.anvay.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AnvayException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
