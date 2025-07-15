package com.grongo.cloud_storage_app.exceptions.storageExceptions;

import org.springframework.http.HttpStatus;

public class FileNotFoundException extends StorageException {
    public FileNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
