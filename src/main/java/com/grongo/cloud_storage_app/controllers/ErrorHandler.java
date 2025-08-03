package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.HttpException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.*;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.InvalidTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.MissingTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenNotFoundException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenUserNotFoundException;
import com.grongo.cloud_storage_app.models.exceptions.ExceptionResponse;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import javax.naming.AuthenticationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ExceptionResponse> generalHttpException(HttpException e){

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                e.getStatus().value(),
                e.getStatus().name(),
                e.getMessage()
        );

        return ResponseEntity
                .status(e.getStatus())
                .body(exceptionResponse);

    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse invalidBody(ConstraintViolationException e){
        return ExceptionResponse.builder()
                .status(400)
                .error(HttpStatus.BAD_REQUEST.name())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler({BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse badCredentials(BadCredentialsException e){
        return ExceptionResponse.builder()
                .status(401)
                .error(HttpStatus.UNAUTHORIZED.name())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> fieldException(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        errors.put("isFields", "true");

        return errors;
    }
}
