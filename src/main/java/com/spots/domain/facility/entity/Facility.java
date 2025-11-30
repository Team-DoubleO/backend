package com.spots.domain.facility.entity;

import com.spots.domain.program.entity.Program;
import com.spots.domain.transport.entity.Transport;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facility", indexes = {
    @Index(name = "idx_facility_location", columnList = "location")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Facility { // 클래스명 변경

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "facility_id")
  private Long id;

  // --- 기본 정보 ---
  @Column(name = "fclty_nm", length = 200, nullable = false)
  private String name;

  @Column(name = "fclty_addr", length = 500)
  private String address;

  @Column(name = "fclty_tel_no", length = 30)
  private String telNo;

  @Column(name = "hmpg_url", length = 500)
  private String hmpgUrl;

  // --- 분류 및 코드 ---
  @Column(name = "induty_cd", length = 30)
  private String indutyCd;
  @Column(name = "induty_nm", length = 200)
  private String indutyNm;

  @Column(name = "fclty_ty_cd", length = 30)
  private String typeCd;
  @Column(name = "fclty_ty_nm", length = 200)
  private String typeNm;

  @Column(name = "ctprvn_cd", length = 30)
  private String ctprvnCd;
  @Column(name = "ctprvn_nm", length = 200)
  private String ctprvnNm;

  @Column(name = "signgu_cd", length = 30)
  private String signguCd;
  @Column(name = "signgu_nm", length = 200)
  private String signguNm;

  @Column(name = "emd_cd", length = 30)
  private String emdCd;
  @Column(name = "emd_nm", length = 200)
  private String emdNm;

  // --- 위치 ---
  @Column(name = "location", columnDefinition = "POINT SRID 4326", nullable = false)
  private Point location;

  // --- 연관 관계 ---
  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Program> programs = new ArrayList<>();

  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Transport> transports = new ArrayList<>();
}