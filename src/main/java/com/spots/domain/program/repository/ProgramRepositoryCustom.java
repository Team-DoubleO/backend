package com.spots.domain.program.repository;

import com.spots.domain.program.dto.request.ProgramInfoServiceRequest;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import org.springframework.data.domain.Slice;

public interface ProgramRepositoryCustom {

  Slice<ProgramInfoResponse> searchPrograms(ProgramInfoServiceRequest req, Long pageSize, Long lastProgramId);
}