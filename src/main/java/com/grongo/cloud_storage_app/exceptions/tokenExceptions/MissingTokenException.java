package com.grongo.cloud_storage_app.exceptions.tokenExceptions;

import org.springframework.http.HttpStatus;

public class MissingTokenException extends TokenException {
    public MissingTokenException() {
        super("Invalid token.", HttpStatus.UNAUTHORIZED, true);
    }
}
