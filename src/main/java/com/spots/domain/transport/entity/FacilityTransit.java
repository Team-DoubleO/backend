package com.spots.domain.transport.entity;

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

@Entity
@Table(name = "facility_transit")
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

  private String pbtrnspFcltyLa;

  private String pbtrnspFcltyLo;

}