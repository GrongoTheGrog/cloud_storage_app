package com.grongo.cloud_storage_app.exceptions;

public class InvalidTokenException extends TokenException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
