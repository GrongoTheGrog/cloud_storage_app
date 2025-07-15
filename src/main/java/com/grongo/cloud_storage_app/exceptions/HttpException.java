package com.grongo.cloud_storage_app.exceptions;

import org.springframework.http.HttpStatus;

public class HttpException extends RuntimeException {
    private final HttpStatus status;
    public HttpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
