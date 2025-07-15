package com.grongo.cloud_storage_app.exceptions.userExceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
