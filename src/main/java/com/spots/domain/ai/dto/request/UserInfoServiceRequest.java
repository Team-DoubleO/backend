package com.spots.domain.ai.dto.request;

import com.spots.domain.program.dto.request.ProgramInfoServiceRequest;
import java.util.List;

public record UserInfoServiceRequest(
    String gender,
    String age,
    Double latitude,
    Double longitude,
    List<String> favorites,
    List<String> weekday,
    List<String> startTime,
    Integer height,
    Integer weight
) {

  public ProgramInfoServiceRequest toProgramInfoServiceRequest() {
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
