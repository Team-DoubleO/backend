package com.spots.domain.program.repository;

import static com.spots.domain.program.entity.Program.splitAndSortDays;
import static com.spots.domain.program.entity.QProgram.program;

import com.querydsl.core.BooleanBuilder;
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

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<ProgramInfoResponse> searchPrograms(
      ProgramInfoServiceRequest req,
      Long pageSize,
      Long lastProgramId,
      Double lastDistance
  ) {

    QProgram p = program;
    BooleanBuilder where = new BooleanBuilder();

    NumberExpression<Double> distance;

    if (req.latitude() != null && req.longitude() != null) {
      Point center = createPoint(req.longitude(), req.latitude());
      where.and(stDWithin(p, center, 10000.0));
      distance = stDistanceKm(p, center);
    } else {
      distance = null;
    }

    if (req.gender() != null && !req.gender().isBlank()) {
      where.and(
          p.genderCategory.eq(req.gender())
              .or(p.genderCategory.eq("전체"))
      );
    }

    if (req.age() != null && !req.age().isBlank()) {
      where.and(p.progrmTrgetCategory.contains(req.age()));
    }

    if (req.favorites() != null && !req.favorites().isEmpty()) {
      BooleanBuilder fav = new BooleanBuilder();
      req.favorites().forEach(f -> fav.or(p.progrmTyNmDetail.contains(f)));
      where.and(fav);
    }

    if (req.weekday() != null && !req.weekday().isEmpty()) {
      BooleanBuilder dayBuilder = new BooleanBuilder();
      req.weekday().forEach(w -> dayBuilder.or(p.progrmEstblWkdayNm.contains(w)));
      where.and(dayBuilder);
    }

    if (req.startTime() != null && !req.startTime().isEmpty()) {
      BooleanBuilder time = new BooleanBuilder();
      req.startTime().forEach(t -> time.or(p.progrmEstblTiznValue.contains(t)));
      where.and(time);
    }

    if (lastDistance != null && lastProgramId != null && distance != null) {
      BooleanBuilder cursor = new BooleanBuilder();

      cursor.or(distance.gt(lastDistance));
      cursor.or(
          distance.eq(lastDistance).and(p.id.gt(lastProgramId))
      );

      where.and(cursor);
    }

    var query = queryFactory
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
        .where(where)
        .orderBy(distance.asc(), p.id.asc());

    var tuples = query
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

  private Point createPoint(double longitude, double latitude) {
    return new GeometryFactory(new PrecisionModel(), 4326)
        .createPoint(new Coordinate(longitude, latitude));
  }

  private BooleanBuilder stDWithin(QProgram p, Point center, double meters) {
    return new BooleanBuilder(
        Expressions.booleanTemplate(
            "function('st_dwithin', {0}, {1}, {2}) = true",
            p.facility.location, center, meters
        )
    );
  }

  private NumberExpression<Double> stDistanceKm(QProgram p, Point center) {
    return Expressions.numberTemplate(
        Double.class,
        "ST_Distance({0}, {1}) / 1000.0",
        p.facility.location, center
    );
  }
}
