package com.spots.global.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  protected ApiResponse<Boolean> handleException(CustomException e) {
    return ApiResponse.error(e.getErrorCode());
  }

}
