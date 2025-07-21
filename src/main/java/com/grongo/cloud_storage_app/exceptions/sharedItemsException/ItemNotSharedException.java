package com.grongo.cloud_storage_app.exceptions.sharedItemsException;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class ItemNotSharedException extends HttpException {
    public ItemNotSharedException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
