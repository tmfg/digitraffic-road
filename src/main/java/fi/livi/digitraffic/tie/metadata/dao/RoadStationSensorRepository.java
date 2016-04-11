package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {
    @Query(value = "select * from road_station_sensor where obsolete = 0 and natural_id < 60000", nativeQuery = true)
    List<RoadStationSensor> findNonObsoleteRoadStationSensors();
}
