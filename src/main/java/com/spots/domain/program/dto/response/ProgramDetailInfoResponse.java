package com.spots.domain.program.dto.response;

import java.util.List;

public record ProgramDetailInfoResponse(
    String programName,
    String programTarget,
    List<String> weekday,
    String startTime,
    Integer price,
    String reservationUrl,
    String category,
    String subCategory,
    String facility,
    String facilityAddress,
    List<TransportData> transportData
) {

  public record TransportData(
      String transportName,
      String transportTime
  ) {

  }
}