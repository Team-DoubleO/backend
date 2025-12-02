package com.spots.domain.program.repository;

import static com.spots.domain.program.entity.Program.splitAndSortDays;
import static com.spots.domain.program.entity.QProgram.program;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spots.domain.program.dto.request.ProgramInfoServiceRequest;
import com.spots.domain.program.dto.response.ProgramInfoResponse;
import com.spots.domain.program.entity.QProgram;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
      Long lastProgramId
  ) {

    QProgram p = program;
    BooleanBuilder where = new BooleanBuilder();

    if (lastProgramId != null) {
      where.and(p.id.lt(lastProgramId));
    }

    NumberExpression<Double> distanceKm = null;
    double radius = 100.0;

    if (req.latitude() != null && req.longitude() != null) {

      where.and(p.facility.fcltyLa.isNotNull())
          .and(p.facility.fcltyLo.isNotNull());

      distanceKm = haversineKm(
          req.latitude(),
          req.longitude(),
          p.facility.fcltyLa,
          p.facility.fcltyLo
      );

      where.and(distanceKm.loe(radius));
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
      for (String f : req.favorites()) {
        fav.or(p.progrmTyNmDetail.contains(f));
      }
      where.and(fav);
    }

    if (req.weekday() != null && !req.weekday().isEmpty()) {
      BooleanBuilder day = new BooleanBuilder();
      for (String w : req.weekday()) {
        day.or(p.progrmEstblWkdayNm.contains(w));
      }
      where.and(day);
    }

    if (req.startTime() != null && !req.startTime().isEmpty()) {
      BooleanBuilder time = new BooleanBuilder();
      for (String t : req.startTime()) {
        time.or(p.progrmEstblTiznValue.contains(t));
      }
      where.and(time);
    }

    var query = queryFactory
        .select(
            p.id,
            p.progrmNm,
            p.progrmEstblWkdayNm,
            p.progrmEstblTiznValue,
            p.facility.fcltyNm,
            p.progrmTyNm,
            p.progrmTyNmDetail
        )
        .from(p)
        .where(where)
        .orderBy(p.id.desc());

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
            t.get(p.progrmTyNmDetail)
        ))
        .toList();

    boolean hasNext = content.size() > pageSize;
    if (hasNext) {
      content = content.subList(0, pageSize.intValue());
    }

    return new SliceImpl<>(content, PageRequest.of(0, pageSize.intValue()), hasNext);
  }

  private NumberExpression<Double> haversineKm(
      double srcLat,
      double srcLon,
      NumberPath<Double> latPath,
      NumberPath<Double> lonPath
  ) {
    return Expressions.numberTemplate(
        Double.class,
        "6371 * acos( cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) "
        + "+ sin(radians({0})) * sin(radians({1})) )",
        srcLat, latPath, lonPath, srcLon
    );
  }
}