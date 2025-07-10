package com.grongo.cloud_storage_app.exceptions.storageExceptions;

public class FolderNotFoundException extends StorageException {
    public FolderNotFoundException(String message) {
        super(message);
    }
}
