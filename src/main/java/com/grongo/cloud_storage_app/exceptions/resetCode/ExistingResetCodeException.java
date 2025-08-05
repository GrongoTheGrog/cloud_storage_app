package com.grongo.cloud_storage_app.exceptions.resetCode;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class ExistingResetCodeException extends HttpException {
    public ExistingResetCodeException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
