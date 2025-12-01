package com.spots.domain.program.dto.request;

import java.util.List;

public record ProgramInfoServiceRequest(
    String gender,
    String age,
    Double latitude,
    Double longitude,
    List<String> favorites,
    List<String> weekday,
    List<String> startTime
){

}
