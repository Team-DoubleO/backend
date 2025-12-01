package com.spots.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Code {

  // SUCCESS
  OK("SUCCESS", "프로그램 상세 페이지 조회에 성공했습니다.");

  private final String status;
  private final String message;

}
