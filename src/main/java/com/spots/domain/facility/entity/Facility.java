package com.spots.domain.facility.entity;

import com.spots.domain.program.entity.Program;
import com.spots.domain.transport.entity.FacilityTransit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "facility")
public class Facility { // 클래스명 변경

  @Id
  @Column(name = "facility_id")
  private Long id;

  private String fcltyNm;

  private String indutyCd;

  private String indutyNm;

  private String fcltyTyCd;

  private String fcltyTyNm;

  private String ctprvnCd;

  private String ctprvnNm;

  private String signguCd;

  private String signguNm;

  private String emdCd;

  private String emdNm;

  private String fcltyAddr;

  private String fcltyTelNo;

  private String fcltyLa;

  private String fcltyLo;

  @OneToMany(mappedBy = "facility", fetch = FetchType.LAZY)
  private List<Program> programs;

  @OneToMany(mappedBy = "facility", fetch = FetchType.LAZY)
  private List<FacilityTransit> transitList;
}