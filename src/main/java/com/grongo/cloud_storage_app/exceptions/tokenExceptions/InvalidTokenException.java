package com.grongo.cloud_storage_app.exceptions.tokenExceptions;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends TokenException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, true);
    }
}
