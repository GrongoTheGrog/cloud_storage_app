package com.grongo.cloud_storage_app.exceptions.sharedItemsException;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class TargetEmailConflictException extends HttpException {
    public TargetEmailConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
