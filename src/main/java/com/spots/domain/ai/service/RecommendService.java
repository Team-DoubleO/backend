package com.spots.domain.ai.service;

import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.request.RecommendLLMRequest.RecommendProgramData;
import com.spots.domain.ai.dto.request.UserInfoServiceRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.domain.program.dto.response.ProgramDetailInfoResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.dto.response.TransportData;
import com.spots.domain.program.repository.ProgramRepository;
import com.spots.domain.program.service.ProgramService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendService {

  private final ProgramService programService;
  private final ProgramRepository programRepository;
  private final RecommendLLMService workoutLLMService;

  public WeeklyRecommendResponse recommendWeeklyRoutine(UserInfoServiceRequest request) {
    List<ProgramInfoResponse> programs = programRepository
        .searchPrograms(request.toProgramInfoServiceRequest(), 40L, null, null).getContent();

    List<RecommendProgramData> recommendProgramDataList = convertToRecommendProgramData(programs);

    RecommendLLMRequest llmRequest = RecommendLLMRequest.from(request, recommendProgramDataList);
    return workoutLLMService.createWeeklyPlan(llmRequest);
  }

  private List<RecommendProgramData> convertToRecommendProgramData(List<ProgramInfoResponse> programs) {
    return programs.stream()
        .map(programInfoResponse -> {
          ProgramDetailInfoResponse programDetailInfoResponse = programService.getProgram(programInfoResponse.programId());
          List<TransportData> transports = programDetailInfoResponse.transportData();

          return new RecommendProgramData(
              programInfoResponse,
              transports
          );
        })
        .toList();
  }
}
