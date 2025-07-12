package com.grongo.cloud_storage_app.models.exceptions;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class ExceptionResponse {

    public ExceptionResponse(
            int status,
            String error,
            String message,
    ){
        this.message = message;
        this.status = status;
        this.timestamp = new Date().toString();
        this.error = error;
    }

    int status;
    String error;
    String message;
    String timestamp;

}
