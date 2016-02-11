package fi.livi.digitraffic.tie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.RoadStation;

@Repository
public interface RoadStationRepository extends JpaRepository<RoadStation, Long>{
}
