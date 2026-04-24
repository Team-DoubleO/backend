package com.spots.domain.ai.service;

import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.request.RecommendLLMRequest.RecommendProgramData;
import com.spots.domain.ai.dto.request.UserInfoServiceRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.dto.response.TransportData;
import com.spots.domain.program.dto.response.TransportDataRawWithFacility;
import com.spots.domain.program.entity.Program;
import com.spots.domain.program.repository.ProgramRepository;
import com.spots.domain.transport.repository.TransitRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {

  private final ProgramRepository programRepository;
  private final TransitRepository transitRepository;
  private final RecommendLLMService recommendLLMService;

  @Async("llmExecutor")
  @Transactional(readOnly = true)
  public CompletableFuture<WeeklyRecommendResponse> recommendWeeklyRoutine(
      UserInfoServiceRequest request
  ) {

    List<ProgramInfoResponse> programs = programRepository
        .searchPrograms(
            request.toProgramInfoServiceRequest(),
            50L,
            null,
            null
        )
        .getContent();

    List<RecommendProgramData> recommendProgramDataList = convertToRecommendProgramData(programs);

    RecommendLLMRequest llmRequest = RecommendLLMRequest.from(request, recommendProgramDataList);
    WeeklyRecommendResponse response = recommendLLMService.createWeeklyPlan(llmRequest);

    return CompletableFuture.completedFuture(response);
  }

  private List<RecommendProgramData> convertToRecommendProgramData(
      List<ProgramInfoResponse> programs
  ) {
    List<Long> programIds = programs.stream()
        .map(ProgramInfoResponse::programId)
        .toList();
    List<Program> programEntities = programRepository.findAllWithFacility(programIds);
    Map<Long, Program> programMap = programEntities.stream()
        .collect(Collectors.toMap(Program::getId, Function.identity()));

    List<Long> facilityIds = programEntities.stream()
        .map(program -> program.getFacility().getId())
        .distinct()
        .toList();

    Map<Long, List<TransportData>> transitMap = transitRepository
        .findTop2TransitByFacilityIds(facilityIds)
        .stream()
        .collect(Collectors.groupingBy(
            TransportDataRawWithFacility::facilityId,
            Collectors.mapping(
                raw -> new TransportData(
                    raw.transportType(),
                    raw.transportName(),
                    raw.transportTime().longValue()
                ),
                Collectors.toList()
            )
        ));

    return programs.stream()
        .map(info -> {
          Program program = programMap.get(info.programId());
          Long facilityId = program.getFacility().getId();
          List<TransportData> transports = transitMap.getOrDefault(facilityId, List.of());
          return new RecommendProgramData(info, transports, info.distance());
        })
        .toList();
  }
}