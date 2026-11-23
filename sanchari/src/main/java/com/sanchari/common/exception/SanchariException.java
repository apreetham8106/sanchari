package com.sanchari.common.exception;

import org.springframework.http.HttpStatus;

public class SanchariException extends RuntimeException {
    private final HttpStatus status;

    public SanchariException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
