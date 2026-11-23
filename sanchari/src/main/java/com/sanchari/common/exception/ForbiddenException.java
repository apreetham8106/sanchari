package com.sanchari.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends SanchariException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
