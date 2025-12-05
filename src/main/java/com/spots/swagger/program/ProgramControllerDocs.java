package com.spots.swagger.program;

import com.spots.domain.program.dto.request.ProgramInfoRequest;
import com.spots.domain.program.dto.response.ProgramDetailInfoResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.global.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Program API", description = "시설 및 프로그램 공공 데이터 조회 관련 API")
public interface ProgramControllerDocs {

  @Operation(
      summary = "카테고리 기반 프로그램 동적 조회 API",
      description = "유저가 선택한 카테고리 및 위치 기반 조건을 토대로 프로그램 목록을 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "요청 데이터 검증 실패",
          content = @Content(
              schema = @Schema(implementation = ApiResponse.class),
              examples = {
                  @ExampleObject(
                      name = "gender 누락",
                      description = "성별 값이 없을 때 발생하는 오류",
                      value = """
                          {
                              "status": "EMPTY_REQUEST_INPUT",
                              "message": "성별은 필수 입력값입니다.",
                              "data": false
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "age 누락",
                      description = "연령대 값이 없을 때 발생하는 오류",
                      value = """
                          {
                              "status": "EMPTY_REQUEST_INPUT",
                              "message": "연령대는 필수 입력값입니다.",
                              "data": false
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "latitude 누락",
                      description = "위도 값이 없을 때 발생하는 오류",
                      value = """
                          {
                              "status": "EMPTY_REQUEST_INPUT",
                              "message": "위도는 필수 입력값입니다.",
                              "data": false
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "longitude 누락",
                      description = "경도 값이 없을 때 발생하는 오류",
                      value = """
                          {
                              "status": "EMPTY_REQUEST_INPUT",
                              "message": "경도는 필수 입력값입니다.",
                              "data": false
                          }
                          """
                  ),
                  @ExampleObject(
                      name = "favorites 비어 있음",
                      description = "선호 운동 리스트가 비어 있을 때 발생하는 오류",
                      value = """
                          {
                              "status": "EMPTY_REQUEST_INPUT",
                              "message": "선호 운동 종목 리스트는 비어 있을 수 없습니다.",
                              "data": false
                          }
                          """
                  )
              }
          )
      )
  })

  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "프로그램 필터링 조건",
      required = true,
      content = @Content(
          schema = @Schema(implementation = ProgramInfoRequest.class),
          examples = {
              @ExampleObject(
                  name = "기본 요청 예시",
                  value = """
                      {
                         "gender": "남성",
                         "age": "성인",
                         "latitude": 37.5665,
                         "longitude": 126.9780,
                         "favorites": [
                           "GX기타", "밸리댄스", "수영"
                         ],
                         "weekday": ["월", "화", "수", "목"],
                         "startTime": ["12:00", "15:00"]
                      }
                      """
              )
          }
      )
  )
  ApiResponse<List<ProgramInfoResponse>> searchPrograms(
      @RequestBody ProgramInfoRequest request,

      @Parameter(
          description = "가져올 데이터 개수 (페이지 사이즈)",
          example = "10"
      )
      @RequestParam("pageSize") Long pageSize,

      @Parameter(
          description = "무한스크롤 시 마지막으로 조회한 Program ID. 첫 요청 시 null",
          example = "12212",
          required = false
      )
      @RequestParam(value = "lastProgramId", required = false) Long lastProgramId
  );

  @Operation(
      summary = "프로그램 상세 조회 API",
      description = "선택한 프로그램 ID에 대한 상세 정보를 조회합니다."
  )
  @Parameter(
      name = "programId",
      description = "조회하고자 하는 프로그램의 고유 ID",
      example = "12212"
  )
  ApiResponse<ProgramDetailInfoResponse> getProgram(
      @PathVariable Long programId
  );
}
