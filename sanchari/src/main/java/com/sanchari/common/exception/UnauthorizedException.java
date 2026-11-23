package com.sanchari.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends SanchariException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
