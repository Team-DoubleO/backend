package com.spots.domain.ai.service;

import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.request.UserInfoServiceRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.repository.ProgramRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendService {

  private final ProgramRepository programRepository;
  private final RecommendLLMService workoutLLMService;

  public WeeklyRecommendResponse recommendWeeklyRoutine(UserInfoServiceRequest request) {
    List<ProgramInfoResponse> programs = programRepository
        .searchPrograms(request.toProgramInfoServiceRequest(), 30L, null).getContent();

    RecommendLLMRequest llmRequest = RecommendLLMRequest.from(request, programs);
    return workoutLLMService.createWeeklyPlan(llmRequest);
  }
}
