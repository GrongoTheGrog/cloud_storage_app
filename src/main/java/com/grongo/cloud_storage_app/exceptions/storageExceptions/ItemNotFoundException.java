package com.grongo.cloud_storage_app.exceptions.storageExceptions;

public class ItemNotFoundException extends StorageException{
    public ItemNotFoundException(String message) {
        super(message);
    }
}
