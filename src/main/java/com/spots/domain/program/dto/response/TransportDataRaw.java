package com.spots.domain.program.dto.response;


public record TransportDataRaw(
    String transportType,
    String transportName,
    Double transportTime
) {

}