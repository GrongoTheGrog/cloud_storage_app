package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.HttpException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.*;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.InvalidTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.MissingTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenNotFoundException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenUserNotFoundException;
import com.grongo.cloud_storage_app.models.exceptions.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import javax.naming.AuthenticationException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(HttpException.class)
    public ExceptionResponse unauthorized(HttpException e){
        return new ExceptionResponse(
                e.getStatus().value(),
                e.getStatus().name(),
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleOtherExceptions(Exception e) {
        e.printStackTrace(); // or use a logger
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(
                        500,
                        "INTERNAL_SERVER_ERROR",
                        e.getMessage()
                ));
    }
}
