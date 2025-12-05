package com.spots.domain.ai.controller;

import com.spots.domain.ai.dto.request.UserInfoRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.domain.ai.service.RecommendService;
import com.spots.global.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

  private final RecommendService recommendService;

  @PostMapping
  public ApiResponse<WeeklyRecommendResponse> recommendWeeklyWorkout(
      @RequestBody UserInfoRequest request
  ) {
    WeeklyRecommendResponse response = recommendService.recommendWeeklyRoutine(request.toServiceRequest());
    return ApiResponse.success(response);
  }
}
