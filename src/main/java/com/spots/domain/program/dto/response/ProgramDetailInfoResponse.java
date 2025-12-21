package com.spots.domain.program.dto.response;

import static com.spots.domain.program.dto.response.TargetGroup.parseToString;
import static com.spots.domain.program.entity.Program.splitAndSortDays;

import com.spots.domain.facility.entity.Facility;
import com.spots.domain.program.entity.Program;
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

  public static ProgramDetailInfoResponse from(Program program, Facility facility, List<TransportDataRaw> transportDataRaws) {

    return new ProgramDetailInfoResponse(
        program.getProgrmNm(),
        parseToString(program.getProgrmTrgetCategory()),
        splitAndSortDays(program.getProgrmEstblWkdayNm()),
        program.getProgrmEstblTiznValue(),
        program.getProgrmPrc().intValue(),
        program.getHmpgUrl(),
        program.getProgrmTyNm(),
        program.getProgrmTyNmDetail(),
        facility.getFcltyNm(),
        facility.getFcltyAddr(),
        transportDataRaws.stream()
            .map(data -> new TransportData(
                data.transportType(),
                data.transportName(),
                data.transportTime().longValue()
            )).toList()
    );
  }
}