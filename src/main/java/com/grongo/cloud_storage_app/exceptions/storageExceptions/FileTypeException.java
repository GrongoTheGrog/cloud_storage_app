package com.grongo.cloud_storage_app.exceptions.storageExceptions;

import org.springframework.http.HttpStatus;

public class FileTypeException extends StorageException {
    public FileTypeException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
