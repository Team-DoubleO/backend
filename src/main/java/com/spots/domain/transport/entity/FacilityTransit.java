package com.spots.domain.transport.entity;

import static lombok.AccessLevel.PROTECTED;

import com.spots.domain.facility.entity.Facility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "facility_transit")
@NoArgsConstructor(access = PROTECTED)
public class FacilityTransit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "transit_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;

  private Integer rank;

  private String pbtrnspFcltySdivNm;

  private Double strtDstncValue;

  private Double wlkgDstncValue;

  private Double wlkgMvmnTime;

  private String bstpSubwaystNm;

  private Double pbtrnspFcltyLa;

  private Double pbtrnspFcltyLo;

}