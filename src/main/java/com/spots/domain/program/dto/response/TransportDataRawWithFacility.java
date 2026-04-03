package com.spots.domain.program.dto.response;

public record TransportDataRawWithFacility(
    Long facilityId,
    String transportType,
    String transportName,
    Double transportTime
) {

}
