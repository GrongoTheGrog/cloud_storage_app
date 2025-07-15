package com.grongo.cloud_storage_app.exceptions.storageExceptions;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends StorageException{
    public ItemNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
