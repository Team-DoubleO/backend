package com.spots.domain.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "target_category")
public class TargetCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "target_category_id")
  private Long id;

  private String progrmTrgetCategory;

  @OneToMany(mappedBy = "targetCategory", fetch = FetchType.LAZY)
  private List<ProgramTargetRel> programs;

}
