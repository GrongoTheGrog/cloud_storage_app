package com.grongo.cloud_storage_app.exceptions.resetCode;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class InvalidCodeException extends HttpException {
    public InvalidCodeException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
