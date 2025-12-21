package com.spots.domain.program.dto.response;

import static java.util.Arrays.stream;

import java.util.EnumSet;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetGroup {

  ADULT("성인"),
  YOUTH("청소년"),
  INFANT("유아"),
  UNKNOWN("분류불가");

  private final String category;

  public static String parseToString(String raw) {
    EnumSet<TargetGroup> groups = parseToSet(raw);

    if (groups.isEmpty()) {
      return "";
    }

    return stream(values())
        .filter(g -> g != UNKNOWN)
        .filter(groups::contains)
        .map(TargetGroup::getCategory)
        .collect(Collectors.joining(", "));
  }

  private static EnumSet<TargetGroup> parseToSet(String raw) {
    if (raw == null || raw.isBlank()) {
      return EnumSet.noneOf(TargetGroup.class);
    }

    EnumSet<TargetGroup> result =
        stream(raw.split("[,\\[\\]/'\"\\s]+"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(token ->
                stream(values())
                    .filter(e -> e.category.equals(token))
            )
            .collect(() -> EnumSet.noneOf(TargetGroup.class),
                EnumSet::add,
                EnumSet::addAll);

    if (result.contains(UNKNOWN)) {
      return EnumSet.of(ADULT, YOUTH, INFANT);
    }

    return result;
  }
}
