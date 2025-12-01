package com.spots.domain.program.service;

import com.spots.domain.program.dto.request.ProgramInfoServiceRequest;
import com.spots.domain.program.dto.response.ProgramDetailInfoResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.repository.ProgramRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramService {

  private final ProgramRepository programRepository;

  public List<ProgramInfoResponse> getPrograms(ProgramInfoServiceRequest request) {

    return null;
  }

  public ProgramDetailInfoResponse getProgram(Long programId) {

    return null;
  }
}
