package fi.livi.digitraffic.tie.dao;

import fi.livi.digitraffic.tie.model.RoadStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadStationRepository extends JpaRepository<RoadStation, Long>{
}
