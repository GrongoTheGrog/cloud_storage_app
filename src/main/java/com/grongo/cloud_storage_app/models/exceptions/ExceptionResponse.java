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
            String message,
            String details
    ){
        this.message = message;
        this.status = status;
        this.details = details;
        this.timestamp = new Date().toString();
    }

    int status;
    String message;
    String details;
    String timestamp;

}
