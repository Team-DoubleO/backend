package com.spots.domain.program.repository;

import static com.spots.domain.program.entity.Program.splitAndSortDays;
import static com.spots.domain.program.entity.QProgram.program;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spots.domain.program.dto.request.ProgramInfoServiceRequest;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.entity.QProgram;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class ProgramRepositoryImpl implements ProgramRepositoryCustom {

  private static final QProgram p = program;
  public static final String ALL = "전체";

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<ProgramInfoResponse> searchPrograms(
      ProgramInfoServiceRequest req,
      Long pageSize,
      Long lastProgramId,
      Double lastDistance
  ) {

    NumberExpression<Double> distance = distanceExpression(req.longitude(), req.latitude());

    var tuples = queryFactory
        .select(
            p.id,
            p.progrmNm,
            p.progrmEstblWkdayNm,
            p.progrmEstblTiznValue,
            p.facility.fcltyNm,
            p.progrmTyNm,
            p.progrmTyNmDetail,
            distance
        )
        .from(p)
        .where(
            withinRadius(req.longitude(), req.latitude(), 10000.0),
            genderEq(req.gender()),
            ageContains(req.age()),
            favoritesIn(req.favorites()),
            weekdayIn(req.weekday()),
            startTimeIn(req.startTime()),
            cursorCondition(distance, lastDistance, lastProgramId)
        )
        .orderBy(distance.asc(), p.id.asc())
        .limit(pageSize + 1)
        .fetch();

    List<ProgramInfoResponse> content = tuples.stream()
        .map(t -> new ProgramInfoResponse(
            t.get(p.id),
            t.get(p.progrmNm),
            splitAndSortDays(t.get(p.progrmEstblWkdayNm)),
            t.get(p.progrmEstblTiznValue),
            t.get(p.facility.fcltyNm),
            t.get(p.progrmTyNm),
            t.get(p.progrmTyNmDetail),
            t.get(distance)
        ))
        .toList();

    boolean hasNext = content.size() > pageSize;

    if (hasNext) {
      content = content.subList(0, pageSize.intValue());
    }

    return new SliceImpl<>(content, PageRequest.of(0, pageSize.intValue()), hasNext);
  }

  private NumberExpression<Double> distanceExpression(Double longitude, Double latitude) {
    if (longitude == null || latitude == null) {
      return null;
    }
    return Expressions.numberTemplate(
        Double.class,
        "ST_Distance({0}, {1}) / 1000.0",
        p.facility.location, createPoint(longitude, latitude)
    );
  }

  private BooleanExpression withinRadius(Double longitude, Double latitude, double meters) {
    if (longitude == null || latitude == null) {
      return null;
    }
    return Expressions.booleanTemplate(
        "function('st_dwithin', {0}, {1}, {2}) = true",
        p.facility.location, createPoint(longitude, latitude), meters
    );
  }

  private BooleanExpression genderEq(String gender) {
    if (gender == null || gender.isBlank()) {
      return null;
    }
    return p.genderCategory.eq(gender).or(p.genderCategory.eq(ALL));
  }

  private BooleanExpression ageContains(String age) {
    if (age == null || age.isBlank()) {
      return null;
    }
    return p.progrmTrgetCategory.contains(age);
  }

  private BooleanExpression favoritesIn(List<String> favorites) {
    if (favorites == null || favorites.isEmpty()) {
      return null;
    }
    return favorites.stream()
        .map(p.progrmTyNmDetail::contains)
        .reduce(BooleanExpression::or)
        .orElse(null);
  }

  private BooleanExpression weekdayIn(List<String> weekday) {
    if (weekday == null || weekday.isEmpty()) {
      return null;
    }
    return weekday.stream()
        .map(p.progrmEstblWkdayNm::contains)
        .reduce(BooleanExpression::or)
        .orElse(null);
  }

  private BooleanExpression startTimeIn(List<String> startTime) {
    if (startTime == null || startTime.isEmpty()) {
      return null;
    }
    return startTime.stream()
        .map(p.progrmEstblTiznValue::contains)
        .reduce(BooleanExpression::or)
        .orElse(null);
  }

  private BooleanExpression cursorCondition(
      NumberExpression<Double> distance, Double lastDistance, Long lastProgramId
  ) {
    if (distance == null || lastDistance == null || lastProgramId == null) {
      return null;
    }
    return distance.gt(lastDistance)
        .or(distance.eq(lastDistance).and(p.id.gt(lastProgramId)));
  }

  private Point createPoint(double longitude, double latitude) {
    return new GeometryFactory(new PrecisionModel(), 4326)
        .createPoint(new Coordinate(longitude, latitude));
  }
}
