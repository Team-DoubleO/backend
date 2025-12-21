package com.spots.domain.ai.service;

import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.request.RecommendLLMRequest.RecommendProgramData;
import com.spots.domain.ai.dto.request.UserInfoServiceRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.domain.program.dto.response.ProgramDetailInfoResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.dto.response.TransportData;
import com.spots.domain.program.entity.Program;
import com.spots.domain.program.repository.ProgramRepository;
import com.spots.domain.program.service.ProgramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Slf4j
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
      log.info("programs size: {}", programs.size());
    List<RecommendProgramData> recommendProgramDataList = convertToRecommendProgramData(programs, request);

    RecommendLLMRequest llmRequest = RecommendLLMRequest.from(request, recommendProgramDataList);
    WeeklyRecommendResponse response = recommendLLMService.createWeeklyPlan(llmRequest);

    return CompletableFuture.completedFuture(response);
  }

    private List<RecommendProgramData> convertToRecommendProgramData(
            List<ProgramInfoResponse> programs,
            UserInfoServiceRequest request
    ) {
        // 성능 측정을 위한 스톱워치 생성
        StopWatch stopWatch = new StopWatch("DataConversionTask");

        // 1. ID 추출
        List<Long> programIds = programs.stream()
                .map(ProgramInfoResponse::programId)
                .toList();

        // 2. 배치 조회 측정
        stopWatch.start("Bulk DB Fetch (findAllById)");
        List<Program> programEntities = programRepository.findAllById(programIds);
        stopWatch.stop();

        // Map 변환
        Map<Long, Program> programMap = programEntities.stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        // 3. 루프 처리 시간 측정 준비
        // stream 내부에서 시간을 누적하기 위해 AtomicLong 사용
        AtomicLong totalGetProgramTime = new AtomicLong(0);
        AtomicLong totalCalcDistanceTime = new AtomicLong(0);

        stopWatch.start("Stream Processing (Loop)");

        List<RecommendProgramData> result = programs.stream()
                .map(programInfoResponse -> {

                    // A. 상세 정보 조회 시간 측정 (가장 의심되는 구간)
                    long startService = System.nanoTime();
                    ProgramDetailInfoResponse programDetailInfoResponse =
                            programService.getProgram(programInfoResponse.programId());
                    long endService = System.nanoTime();
                    totalGetProgramTime.addAndGet(endService - startService); // 누적

                    List<TransportData> transports = programDetailInfoResponse.transportData();

                    Program program = programMap.get(programInfoResponse.programId());

                    // B. 거리 계산 시간 측정
                    double distance = 0.0;
                    if (program != null && program.getFacility() != null) {
                        long startCalc = System.nanoTime();
                        distance = calculateDistance(
                                request.latitude(),
                                request.longitude(),
                                program.getFacility().getFcltyLa(),
                                program.getFacility().getFcltyLo()
                        );
                        long endCalc = System.nanoTime();
                        totalCalcDistanceTime.addAndGet(endCalc - startCalc);
                    }

                    return new RecommendProgramData(
                            programInfoResponse,
                            transports,
                            distance
                    );
                })
                .toList();

        stopWatch.stop();

        // 4. 결과 로그 출력
        log.info("================ 성능 측정 결과 ================");
        log.info(stopWatch.prettyPrint()); // 전체적인 요약 출력
        log.info(">> [상세] programService.getProgram() 총 소요 시간: {} ms", totalGetProgramTime.get() / 1_000_000);
        log.info(">> [상세] 거리 계산 총 소요 시간: {} ms", totalCalcDistanceTime.get() / 1_000_000);
        log.info("==============================================");

        return result;
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
