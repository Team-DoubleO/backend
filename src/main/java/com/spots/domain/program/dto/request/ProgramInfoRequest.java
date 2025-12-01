package com.spots.domain.program.dto.request;

import java.util.List;

public record ProgramInfoRequest(
    String gender,
    String age,
    Double latitude,
    Double longitude,
    List<String> favorites,
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
