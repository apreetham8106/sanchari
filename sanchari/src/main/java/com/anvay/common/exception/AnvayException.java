package com.anvay.common.exception;

import org.springframework.http.HttpStatus;

public class AnvayException extends RuntimeException {
    private final HttpStatus status;

    public AnvayException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
