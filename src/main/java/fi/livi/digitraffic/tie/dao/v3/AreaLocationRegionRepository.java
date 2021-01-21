package fi.livi.digitraffic.tie.dao.v3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.AreaLocationRegion;

@Repository
public interface AreaLocationRegionRepository extends JpaRepository<AreaLocationRegion, Long> {

}
