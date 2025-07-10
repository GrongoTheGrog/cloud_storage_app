package com.grongo.cloud_storage_app.exceptions.tokenExceptions;

public class MissingTokenException extends TokenException {
    public MissingTokenException(String message) {
        super(message);
    }
}
