package com.grongo.cloud_storage_app.exceptions.tokenExceptions;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class TokenException extends HttpException {
    public TokenException(String message, HttpStatus status, boolean isRefreshRequested) {
        super(message, status, isRefreshRequested);
    }
}
