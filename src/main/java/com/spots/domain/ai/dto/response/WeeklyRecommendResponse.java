package com.spots.domain.ai.dto.response;

import java.util.List;


public record WeeklyRecommendResponse(
    String planRange,
    String subtitle,
    String focus,
    Integer targetSessions,
    Integer totalMinutes,
    Integer estimatedCalories,
    List<WorkoutSession> schedule
) {

  public record WorkoutSession(
      String dayKo,
      String dayEn,
      String time,
      String place,
      String type,
      String distanceWalk,
      String tag
  ) {

  }
}
