package com.example.orders.api;

import com.example.orders.application.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(OrderNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
  }

  public record ApiError(String message) {
  }
}
