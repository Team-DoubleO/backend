package com.spots.domain.program.dto.response;

import java.util.List;

public record ProgramInfoResponse(
    Long programId,
    String programName,
    List<String> weekday,
    String startTime,
    String facility,
    String category,
    String subCategory
) {

}
