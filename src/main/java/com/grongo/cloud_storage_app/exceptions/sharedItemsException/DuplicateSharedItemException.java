package com.grongo.cloud_storage_app.exceptions.sharedItemsException;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class DuplicateSharedItemException extends HttpException {
    public DuplicateSharedItemException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
