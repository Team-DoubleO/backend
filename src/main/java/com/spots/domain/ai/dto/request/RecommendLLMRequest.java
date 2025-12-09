package com.spots.domain.ai.dto.request;

import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.dto.response.TransportData;
import java.util.List;

public record RecommendLLMRequest(
    UserInfo userInfo,
    List<RecommendProgramData> programs
) {

  public static RecommendLLMRequest from(
      UserInfoServiceRequest request,
      List<RecommendProgramData> programs
  ) {
    return new RecommendLLMRequest(
        new UserInfo(
            request.gender(),
            request.age(),
            request.latitude(),
            request.longitude(),
            request.favorites(),
            request.weekday(),
            request.startTime(),
            request.height(),
            request.weight()
        ),
        programs
    );
  }

  public record RecommendProgramData(
      ProgramInfoResponse programInfoResponse,
      List<TransportData> transportData,
      Double distance
  ) {

  }

  public record UserInfo(
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

  }
}