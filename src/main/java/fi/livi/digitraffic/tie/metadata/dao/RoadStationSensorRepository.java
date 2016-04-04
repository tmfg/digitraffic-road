package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {
    List<RoadStationSensor> findByObsoleteDateIsNull();
}