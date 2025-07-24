package com.grongo.cloud_storage_app.exceptions.tags;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class TagConflictException extends HttpException {
  public TagConflictException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
