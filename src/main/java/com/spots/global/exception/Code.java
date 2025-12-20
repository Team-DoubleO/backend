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
  EMPTY_REQUEST_INPUT("EMPTY_REQUEST_INPUT", "입력값이 비어있습니다."),

  // LLM Service Error
  LLM_INTERRUPT_ERROR("LLM_INTERRUPT_ERROR", "LLM 서비스 응답 대기 중 인터럽트가 발생했습니다."),
  LLM_SERVICE_ERROR("LLM_SERVICE_ERROR", "LLM 서비스 처리 중 오류가 발생했습니다."),

  // Json Response Error
  JSON_CONVERSION_ERROR("JSON_CONVERSION_ERROR", "JSON 변환에 실패했습니다."),
  INVALID_JSON_RESPONSE("INVALID_JSON_RESPONSE", "LLM 응답 JSON 파싱에 실패했습니다."),

  // Prompt Error
  PROMPT_LOADING_ERROR("PROMPT_LOADING_ERROR", "프롬프트 파일 로딩에 실패했습니다.");

  private final String status;
  private final String message;
}
