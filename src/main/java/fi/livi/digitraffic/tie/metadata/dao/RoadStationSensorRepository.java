package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {

    @Query(value =
           "SELECT s\n" +
           "FROM RoadStationSensor s\n" +
           "WHERE s.obsolete = false\n" +
           "  AND s.roadStationType = ?1\n" +
           "  AND EXISTS (\n" +
           "     FROM AllowedRoadStationSensor allowed\n" +
           "     WHERE allowed.naturalId = s.naturalId\n" +
           "       AND allowed.roadStationType = s.roadStationType\n" +
           "  )" +
           "ORDER BY s.naturalId")
    List<RoadStationSensor> findByRoadStationTypeAndObsoleteFalseAndAllowed(RoadStationType roadStationType);

    List<RoadStationSensor> findByRoadStationType(RoadStationType roadStationType);
}
