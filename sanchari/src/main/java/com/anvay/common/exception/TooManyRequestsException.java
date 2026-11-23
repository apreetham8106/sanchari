package com.anvay.common.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends AnvayException {
    public TooManyRequestsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
