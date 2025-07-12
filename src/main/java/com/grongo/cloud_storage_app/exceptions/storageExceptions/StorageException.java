package com.grongo.cloud_storage_app.exceptions.storageExceptions;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class StorageException extends HttpException {
    public StorageException(String message, HttpStatus status) {
        super(message, status);
    }
}
