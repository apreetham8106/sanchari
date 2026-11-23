package com.anvay.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AnvayException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
