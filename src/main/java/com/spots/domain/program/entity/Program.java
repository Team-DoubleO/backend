package com.spots.domain.program.entity;

import com.spots.domain.facility.entity.Facility;
import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "program")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Program { // 클래스명 변경

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "program_id")
  private Long id;

  // 부모 엔티티 (Facility)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id", nullable = false)
  private Facility facility;

  // --- 기본 정보 ---
  @Column(name = "progrm_nm", length = 200, nullable = false)
  private String name;

  @Column(name = "progrm_ty_nm", length = 200)
  private String typeName;

  @Column(name = "progrm_ty_nm_detail", length = 200)
  private String typeDetail;

  @Column(name = "target_nm", length = 200)
  private String targetName;

  @Column(name = "target_category", length = 500)
  private String targetCategory;

  @Column(name = "gender_category", length = 50)
  private String genderCategory;

  // --- 날짜 정보 (LocalDate) ---
  @Column(name = "begin_date")
  private LocalDate beginDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  // --- 기타 정보 ---
  @Column(name = "time_info", length = 200)
  private String timeInfo;

  @Column(name = "recruit_count")
  private Integer recruitCount;

  @Column(name = "price")
  private Integer price;

  @Column(name = "price_type", length = 200)
  private String priceType;

  // --- 요일 정보 (별도 테이블: program_days) ---
  @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.LAZY)
  @CollectionTable(
      name = "program_days", // 테이블명 변경
      joinColumns = @JoinColumn(name = "program_id")
  )
  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week")
  @Builder.Default
  private Set<DayOfWeek> operatingDays = new HashSet<>();

  // --- Helper Method: 날짜 파싱 ---
  public void setDatesFromString(String beginStr, String endStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    try {
      if (beginStr != null && !beginStr.isBlank()) {
        this.beginDate = LocalDate.parse(beginStr.trim(), formatter);
      }
      if (endStr != null && !endStr.isBlank()) {
        this.endDate = LocalDate.parse(endStr.trim(), formatter);
      }
    } catch (DateTimeParseException e) {
      // 예외 처리 로직 (로깅 등)
    }
  }

  // --- Helper Method: 요일 파싱 ---
  public void setOperatingDaysFromString(String koreanDays) {
    this.operatingDays.clear();
    if (koreanDays == null || koreanDays.isBlank()) return;

    if (koreanDays.contains("월")) this.operatingDays.add(DayOfWeek.MONDAY);
    if (koreanDays.contains("화")) this.operatingDays.add(DayOfWeek.TUESDAY);
    if (koreanDays.contains("수")) this.operatingDays.add(DayOfWeek.WEDNESDAY);
    if (koreanDays.contains("목")) this.operatingDays.add(DayOfWeek.THURSDAY);
    if (koreanDays.contains("금")) this.operatingDays.add(DayOfWeek.FRIDAY);
    if (koreanDays.contains("토")) this.operatingDays.add(DayOfWeek.SATURDAY);
    if (koreanDays.contains("일")) this.operatingDays.add(DayOfWeek.SUNDAY);
  }

  // --- Helper Method: 타겟 정제 ---
  public void setCleanTargetCategory(String rawCategory) {
    if (rawCategory != null) {
      this.targetCategory = rawCategory.replace("[", "")
          .replace("]", "")
          .replace("'", "")
          .replace("\"", "");
    } else {
      this.targetCategory = rawCategory;
    }
  }

  // --- 연관관계 편의 메서드 ---
  public void setFacility(Facility facility) {
    this.facility = facility;
    if (!facility.getPrograms().contains(this)) {
      facility.getPrograms().add(this);
    }
  }
}