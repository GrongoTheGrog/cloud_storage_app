package com.grongo.cloud_storage_app.exceptions.userExceptions;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class UserException extends HttpException {
    public UserException(String message, HttpStatus status) {
        super(message, status);
    }
}
