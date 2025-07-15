package com.grongo.cloud_storage_app.exceptions.storageExceptions;

import org.springframework.http.HttpStatus;

public class ConflictStorageException extends StorageException {
    public ConflictStorageException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
