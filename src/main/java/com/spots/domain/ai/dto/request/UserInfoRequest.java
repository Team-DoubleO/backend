package com.spots.domain.ai.dto.request;

import java.util.List;

public record UserInfoRequest(
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

  public UserInfoServiceRequest toServiceRequest() {
    return new UserInfoServiceRequest(
        this.gender,
        this.age,
        this.latitude,
        this.longitude,
        this.favorites,
        this.weekday,
        this.startTime,
        this.height,
        this.weight
    );
  }
}
