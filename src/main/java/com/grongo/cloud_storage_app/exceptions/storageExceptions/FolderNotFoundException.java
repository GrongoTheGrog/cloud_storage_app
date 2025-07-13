package com.grongo.cloud_storage_app.exceptions.storageExceptions;

import org.springframework.http.HttpStatus;

public class FolderNotFoundException extends StorageException {
    public FolderNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
