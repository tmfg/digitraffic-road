package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, Long> {

    @EntityGraph("weatherStation")
    @Override
    List<WeatherStation> findAll();

    List<WeatherStation> findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId();

    WeatherStation findByLotjuId(long lotjuId);

    List<WeatherStation> findByLotjuIdIn(List<Long> weatherStationLotjuIds);

    List<WeatherStation> findByLotjuIdIsNull();
}
