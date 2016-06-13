package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {

    @Query(value =
            "SELECT S.*\n" +
            "FROM ROAD_STATION_SENSOR S\n" +
            "WHERE S.OBSOLETE = 0\n" +
            "  AND S.NATURAL_ID < 60000\n" +
            "ORDER BY S.NATURAL_ID",
           nativeQuery = true)
    List<RoadStationSensor> findNonObsoleteRoadStationSensors();
}
