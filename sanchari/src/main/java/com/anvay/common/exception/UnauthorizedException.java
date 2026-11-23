package com.anvay.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AnvayException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
