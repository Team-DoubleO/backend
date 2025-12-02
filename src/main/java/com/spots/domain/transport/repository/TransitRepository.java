package com.spots.domain.transport.repository;

import com.spots.domain.program.dto.response.TransportDataRaw;
import com.spots.domain.transport.entity.FacilityTransit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransitRepository extends JpaRepository<FacilityTransit, Long> {

  @Query(value = """
          SELECT DISTINCT ON (rank) 
                 bstp_subwayst_nm AS transportName,
                 (wlkg_mvmn_time / 60) AS transportTime
          FROM facility_transit
          WHERE facility_id = :facilityId
            AND rank IN (1, 2)
          ORDER BY rank, wlkg_mvmn_time ASC
      """, nativeQuery = true)
  List<TransportDataRaw> findTop2Transit(@Param("facilityId") Long facilityId);
}