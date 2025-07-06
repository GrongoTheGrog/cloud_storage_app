package com.grongo.cloud_storage_app.exceptions;

public class MissingTokenException extends TokenException {
    public MissingTokenException(String message) {
        super(message);
    }
}
