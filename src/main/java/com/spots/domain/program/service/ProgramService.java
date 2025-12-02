package com.spots.domain.program.service;

import static com.spots.global.exception.Code.NOT_FOUND_PROGRAM;

import com.spots.domain.facility.entity.Facility;
import com.spots.domain.facility.repository.FacilityRepository;
import com.spots.domain.program.dto.request.ProgramInfoServiceRequest;
import com.spots.domain.program.dto.response.ProgramDetailInfoResponse;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.dto.response.TransportDataRaw;
import com.spots.domain.program.entity.Program;
import com.spots.domain.program.repository.ProgramRepository;
import com.spots.domain.transport.repository.TransitRepository;
import com.spots.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

  private final ProgramRepository programRepository;
  private final FacilityRepository facilityRepository;
  private final TransitRepository transitRepository;

  public List<ProgramInfoResponse> searchPrograms(ProgramInfoServiceRequest request, Long pageSize, Long lastProgramId) {
    return programRepository.searchPrograms(request, pageSize, lastProgramId).getContent();
  }

  public ProgramDetailInfoResponse getProgram(Long programId) {

    Program program = programRepository.findById(programId).orElseThrow(
        () -> new CustomException(NOT_FOUND_PROGRAM)
    );

    Long facilityId = program.getFacility().getId();
    Facility facility = facilityRepository.findById(facilityId).orElseThrow(
        () -> new CustomException(NOT_FOUND_PROGRAM)
    );

    List<TransportDataRaw> top2Transit = transitRepository.findTop2Transit(facilityId);

    return ProgramDetailInfoResponse.from(program, facility, top2Transit);
  }
}
