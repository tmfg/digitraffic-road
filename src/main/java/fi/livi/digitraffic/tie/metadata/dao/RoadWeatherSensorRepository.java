package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadWeatherSensor;

@Repository
public interface RoadWeatherSensorRepository extends JpaRepository<RoadWeatherSensor, Long> {

    @EntityGraph("roadWeatherSensor")
    @Override
    List<RoadWeatherSensor> findAll();
}
