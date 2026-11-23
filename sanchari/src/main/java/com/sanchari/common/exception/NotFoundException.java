package com.sanchari.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends SanchariException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
