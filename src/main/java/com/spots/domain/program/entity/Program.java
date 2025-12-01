package com.spots.domain.program.entity;

import com.spots.domain.category.entity.ProgramTargetRel;
import com.spots.domain.facility.entity.Facility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "program")
public class Program {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "program_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;

  private String progrmTyNm;

  private String progrmTyNmDetail;

  private String progrmNm;

  private String progrmTrgetNm;

  private String progrmBeginDe;

  private String progrmEndDe;

  private String progrmEstblWkdayNm;

  private String progrmEstblTiznValue;

  private Integer progrmRcritNmprCo;

  private Double progrmPrc;

  private String progrmPrcTyNm;

  private String hmpgUrl;

  private String genderCategory;

  private String progrmTrgetCategory;

  @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
  private List<ProgramTargetRel> targetCategories;

}