package com.spots.domain.program.repository;

import com.spots.domain.program.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramRepositoryCustom {

}
