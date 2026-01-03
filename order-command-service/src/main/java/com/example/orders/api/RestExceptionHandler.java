package com.example.orders.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
  }

  public record ApiError(String message) {
  }
}
