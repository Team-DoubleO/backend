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
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendService {

  private final ProgramService programService;
  private final ProgramRepository programRepository;
  private final RecommendLLMService workoutLLMService;

  private final double EARTH_RADIUS_KM = 6371.0;

  public WeeklyRecommendResponse recommendWeeklyRoutine(UserInfoServiceRequest request) {
    List<ProgramInfoResponse> programs = programRepository
        .searchPrograms(request.toProgramInfoServiceRequest(), 50L, null, null).getContent();

    List<RecommendProgramData> recommendProgramDataList = convertToRecommendProgramData(programs, request);

    RecommendLLMRequest llmRequest = RecommendLLMRequest.from(request, recommendProgramDataList);
    return workoutLLMService.createWeeklyPlan(llmRequest);
  }

  private List<RecommendProgramData> convertToRecommendProgramData(
      List<ProgramInfoResponse> programs,
      UserInfoServiceRequest request
  ) {
    return programs.stream()
        .map(programInfoResponse -> {
          ProgramDetailInfoResponse programDetailInfoResponse = programService.getProgram(programInfoResponse.programId());
          List<TransportData> transports = programDetailInfoResponse.transportData();

          AtomicReference<Double> distance = new AtomicReference<>(0.0);
          programRepository.findById(programInfoResponse.programId()).ifPresent(program -> {
            Double fcltyLa = program.getFacility().getFcltyLa();
            Double fcltyLo = program.getFacility().getFcltyLo();
            Double userLa = request.latitude();
            Double userLo = request.longitude();

            distance.set(calculateDistance(userLa, userLo, fcltyLa, fcltyLo));
          });

          return new RecommendProgramData(
              programInfoResponse,
              transports,
              distance.get()
          );
        })
        .toList();
  }

  private Double calculateDistance(Double userLa, Double userLo, Double fcltyLa, Double fcltyLo) {
    final double R = EARTH_RADIUS_KM;

    double lat1 = Math.toRadians(userLa);
    double lon1 = Math.toRadians(userLo);
    double lat2 = Math.toRadians(fcltyLa);
    double lon2 = Math.toRadians(fcltyLo);

    double diffLat = lat2 - lat1;
    double diffLon = lon2 - lon1;

    double a = Math.pow(Math.sin(diffLat / 2), 2)
               + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(diffLon / 2), 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return valueOf(R * c)
        .setScale(2, HALF_UP)
        .doubleValue();
  }
}
