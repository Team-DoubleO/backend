package com.spots.domain.program.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProgramInfoRequest(
    @NotBlank(message = "성별은 필수 입력값입니다.") String gender,
    @NotBlank(message = "연령대는 필수 입력값입니다.") String age,
    @NotNull(message = "위도는 필수 입력값입니다.") Double latitude,
    @NotNull(message = "경도는 필수 입력값입니다.") Double longitude,
    @NotEmpty(message = "선호 운동 종목 리스트는 비어 있을 수 없습니다.") List<String> favorites,
    List<String> weekday,
    List<String> startTime
) {

  public ProgramInfoServiceRequest toServiceRequest() {
    return new ProgramInfoServiceRequest(
        this.gender,
        this.age,
        this.latitude,
        this.longitude,
        this.favorites,
        this.weekday,
        this.startTime
    );
  }
}
