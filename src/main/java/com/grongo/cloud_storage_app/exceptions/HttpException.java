package com.grongo.cloud_storage_app.exceptions;

import org.springframework.http.HttpStatus;

public class HttpException extends RuntimeException {
    private final HttpStatus status;
    private boolean isRefreshNeeded = false;

    public HttpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpException(String message, HttpStatus status, boolean isRefreshNeeded) {
        super(message);
        this.status = status;
        this.isRefreshNeeded = isRefreshNeeded;
    }



    public HttpStatus getStatus() {
        return status;
    }

    public boolean isRefreshNeeded(){
        return isRefreshNeeded;
    }
}
