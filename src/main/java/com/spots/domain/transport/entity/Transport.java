package com.spots.domain.transport.entity;

import com.spots.domain.facility.entity.Facility;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "transport")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transport { // 클래스명 변경

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "transport_id")
  private Long id;

  // 부모 엔티티 (Facility)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id", nullable = false)
  private Facility facility;

  // --- 교통 정보 데이터 ---
  @Column(name = "rank_num", nullable = false)
  private Integer rank; // 1위, 2위 등 순위 정보

  @Column(name = "transport_type", length = 100)
  private String type; // 버스, 지하철 등

  @Column(name = "station_name", length = 200)
  private String stationName; // 정류장/역 이름

  @Column(name = "straight_distance")
  private Double straightDistance; // 직선 거리

  @Column(name = "walking_distance")
  private Double walkingDistance; // 도보 거리

  @Column(name = "walking_time")
  private Double walkingTime; // 도보 시간

  // 교통 시설 위치 (Point)
  @Column(name = "location", columnDefinition = "POINT SRID 4326")
  private Point location;

  // --- 연관관계 편의 메서드 ---
  public void setFacility(Facility facility) {
    this.facility = facility;
    // Facility 엔티티의 리스트 이름이 transports라고 가정
    if (!facility.getTransports().contains(this)) {
      facility.getTransports().add(this);
    }
  }
}