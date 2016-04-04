package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadWeatherStationRepository extends JpaRepository<RoadWeatherStation, Long> {

    @EntityGraph("roadWeatherStation")
    @Override
    List<RoadWeatherStation> findAll();
}
