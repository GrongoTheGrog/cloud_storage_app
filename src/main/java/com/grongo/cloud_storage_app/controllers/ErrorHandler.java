package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.storageExceptions.*;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.InvalidTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.MissingTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenNotFoundException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenUserNotFoundException;
import com.grongo.cloud_storage_app.models.exceptions.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import javax.naming.AuthenticationException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
            AuthenticationException.class,
            InvalidTokenException.class,
            TokenNotFoundException.class,
            MissingTokenException.class
    })
    public ExceptionResponse unauthorized(RuntimeException e){
        return new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(),
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({
            SQLIntegrityConstraintViolationException.class,
            ConflictStorageException.class
    })
    public ExceptionResponse conflict(RuntimeException e){
        return new ExceptionResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.name(),
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            AmazonException.class,
            FileTypeException.class,
            StorageException.class
    })
    public ExceptionResponse serverError(RuntimeException e){
        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                e.getMessage()
        );
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            FileNotFoundException.class,
            FolderNotFoundException.class,
            ItemNotFoundException.class
    })
    public ExceptionResponse notFound(RuntimeException e){
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.name(),
                e.getMessage()
        );
    }




}
