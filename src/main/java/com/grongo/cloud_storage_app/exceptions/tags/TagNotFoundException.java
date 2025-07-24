package com.grongo.cloud_storage_app.exceptions.tags;

import com.grongo.cloud_storage_app.exceptions.HttpException;
import org.springframework.http.HttpStatus;

public class TagNotFoundException extends HttpException {
  public TagNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
