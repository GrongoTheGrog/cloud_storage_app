package com.grongo.cloud_storage_app.exceptions.tokenExceptions;

import org.springframework.http.HttpStatus;

public class TokenUserNotFoundException extends TokenException {
    public TokenUserNotFoundException(String message, HttpStatus status) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
