package com.grongo.cloud_storage_app.exceptions.storageExceptions;

public class FolderNotFoundException extends ItemNotFoundException {
    public FolderNotFoundException(String message) {
        super(message);
    }
}
