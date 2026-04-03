package com.spots.domain.program.repository;

import com.spots.domain.program.entity.Program;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramRepositoryCustom {

  @Query("SELECT p FROM Program p JOIN FETCH p.facility WHERE p.id IN :ids")
  List<Program> findAllWithFacility(@Param("ids") List<Long> ids);

}
