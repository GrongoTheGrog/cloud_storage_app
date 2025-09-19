package com.grongo.cloud_storage_app.models.exceptions;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class ExceptionResponse {

    public ExceptionResponse(
            int status,
            String error,
            String message,
            boolean isRefreshNeeded
    ){
        this.message = message;
        this.status = status;
        timestamp = new Date().toString();
        this.error = error;
        this.isRefreshNeeded = isRefreshNeeded;
    }

    int status;
    String error;
    String message;
    String timestamp;
    boolean isRefreshNeeded;

}
