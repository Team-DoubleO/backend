package com.spots.domain.program.controller;

import com.spots.domain.program.dto.request.ProgramInfoRequest;
import com.spots.domain.program.dto.response.ProgramDetailInfoResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.service.ProgramService;
import com.spots.global.exception.ApiResponse;
import com.spots.swagger.program.ProgramControllerDocs;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/programs")
public class ProgramController implements ProgramControllerDocs {

  private final ProgramService programService;

  @PostMapping
  public ApiResponse<List<ProgramInfoResponse>> searchPrograms(
      @Valid @RequestBody ProgramInfoRequest request,
      @RequestParam("pageSize") Long pageSize,
      @RequestParam(value = "lastProgramId", required = false) Long lastProgramId
  ) {
    List<ProgramInfoResponse> responses = programService
        .searchPrograms(request.toServiceRequest(), pageSize, lastProgramId);
    return ApiResponse.success(responses);
  }

  @GetMapping("/{programId}")
  public ApiResponse<ProgramDetailInfoResponse> getProgram(@PathVariable Long programId) {
    ProgramDetailInfoResponse response = programService.getProgram(programId);
    return ApiResponse.success(response);
  }
}
