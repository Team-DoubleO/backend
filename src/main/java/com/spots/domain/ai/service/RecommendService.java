package com.spots.domain.ai.service;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

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
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendService {

  private final ProgramService programService;
  private final ProgramRepository programRepository;
  private final RecommendLLMService recommendLLMService;

  private static final double EARTH_RADIUS_KM = 6371.0;

  @Async("llmExecutor")
  @Transactional(readOnly = true)
  public CompletableFuture<WeeklyRecommendResponse> recommendWeeklyRoutine(
      UserInfoServiceRequest request
  ) {

    List<ProgramInfoResponse> programs =
        programRepository
            .searchPrograms(
                request.toProgramInfoServiceRequest(),
                50L,
                null,
                null
            )
            .getContent();

    List<RecommendProgramData> recommendProgramDataList = convertToRecommendProgramData(programs, request);

    RecommendLLMRequest llmRequest = RecommendLLMRequest.from(request, recommendProgramDataList);
    WeeklyRecommendResponse response = recommendLLMService.createWeeklyPlan(llmRequest);

    return CompletableFuture.completedFuture(response);
  }

  private List<RecommendProgramData> convertToRecommendProgramData(
      List<ProgramInfoResponse> programs,
      UserInfoServiceRequest request
  ) {
    return programs.stream()
        .map(programInfoResponse -> {

          ProgramDetailInfoResponse programDetailInfoResponse =
              programService.getProgram(programInfoResponse.programId());

          List<TransportData> transports =
              programDetailInfoResponse.transportData();

          double distance =
              programRepository.findById(programInfoResponse.programId())
                  .map(program -> calculateDistance(
                      request.latitude(),
                      request.longitude(),
                      program.getFacility().getFcltyLa(),
                      program.getFacility().getFcltyLo()
                  ))
                  .orElse(0.0);

          return new RecommendProgramData(
              programInfoResponse,
              transports,
              distance
          );
        })
        .toList();
  }

  private Double calculateDistance(
      Double userLa,
      Double userLo,
      Double fcltyLa,
      Double fcltyLo
  ) {
    final double R = EARTH_RADIUS_KM;

    double lat1 = Math.toRadians(userLa);
    double lon1 = Math.toRadians(userLo);
    double lat2 = Math.toRadians(fcltyLa);
    double lon2 = Math.toRadians(fcltyLo);

    double diffLat = lat2 - lat1;
    double diffLon = lon2 - lon1;

    double a =
        Math.pow(Math.sin(diffLat / 2), 2)
        + Math.cos(lat1)
          * Math.cos(lat2)
          * Math.pow(Math.sin(diffLon / 2), 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return valueOf(R * c)
        .setScale(2, HALF_UP)
        .doubleValue();
  }
}
