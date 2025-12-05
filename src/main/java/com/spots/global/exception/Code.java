package com.spots.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Code {

  // SUCCESS
  OK("SUCCESS", "요청에 성공했습니다."),

  // Program
  NOT_FOUND_PROGRAM("NOT_FOUND_PROGRAM", "존재하지 않는 프로그램입니다."),

  // Facility
  NOT_FOUND_FACILITY("NOT_FOUND_FACILITY", "존재하지 않는 시설입니다."),

  // User Input Error
  EMPTY_REQUEST_INPUT("EMPTY_REQUEST_INPUT", "입력값이 비어있습니다.");

  private final String status;
  private final String message;

}
