package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.InvalidTokenException;
import com.grongo.cloud_storage_app.exceptions.MissingTokenException;
import com.grongo.cloud_storage_app.exceptions.TokenException;
import com.grongo.cloud_storage_app.exceptions.TokenUserNotFoundException;
import com.grongo.cloud_storage_app.models.dto.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ExceptionResponse authenticationException(AuthenticationException e){
        return new ExceptionResponse(
                401,
                e.getMessage(),
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ExceptionResponse authenticationException(SQLIntegrityConstraintViolationException e){
        return new ExceptionResponse(
                409,
                "Conflict in database.",
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidTokenException.class)
    public ExceptionResponse authenticationException(InvalidTokenException e){
        return new ExceptionResponse(
                401,
                "Invalid token.",
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(TokenUserNotFoundException.class)
    public ExceptionResponse authenticationException(TokenUserNotFoundException e){
        return new ExceptionResponse(
                401,
                "User not found with provided token.",
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(MissingTokenException.class)
    public ExceptionResponse missingTokenException(MissingTokenException e){
        return new ExceptionResponse(
                401,
                "Missing token.",
                e.getMessage()
        );
    }
}
