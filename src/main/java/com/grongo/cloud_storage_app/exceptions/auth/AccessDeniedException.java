package com.grongo.cloud_storage_app.exceptions.auth;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends HttpException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
