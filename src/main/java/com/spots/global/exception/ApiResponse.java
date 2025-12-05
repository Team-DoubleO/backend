package com.spots.global.exception;

import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = PRIVATE)
public class ApiResponse<T> {

  private final String status;
  private final String message;
  private final T data;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponseBuilder<T>()
        .status(Code.OK.getStatus())
        .message(Code.OK.getMessage())
        .data(data).build();
  }

  public static <T> ApiResponse<Map<String, T>> success(String key, T data) {
    return new ApiResponseBuilder<Map<String, T>>()
        .status(Code.OK.getStatus())
        .message(Code.OK.getMessage())
        .data(Map.of(key, data)).build();
  }

  public static ApiResponse<Boolean> error(Code errorCode) {
    return new ApiResponseBuilder<Boolean>()
        .status(errorCode.getStatus())
        .message(errorCode.getMessage())
        .data(false).build();
  }

  public static ApiResponse<Boolean> error(Code errorCode, String errorMessage) {
    return new ApiResponseBuilder<Boolean>()
        .status(errorCode.getStatus())
        .message(errorMessage)
        .data(false).build();
  }
}
