package com.sanchari.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends SanchariException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
