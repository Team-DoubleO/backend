package com.spots.global.exception;

import static com.spots.global.exception.Code.EMPTY_REQUEST_INPUT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  protected ApiResponse<Boolean> handleException(CustomException e) {
    return ApiResponse.error(e.getErrorCode());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ApiResponse<Boolean> handleValidationException(MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult()
        .getAllErrors()
        .getFirst()
        .getDefaultMessage();

    log.error("MethodArgumentNotValidException: {}", errorMessage);
    return ApiResponse.error(EMPTY_REQUEST_INPUT, errorMessage);
  }

}
