package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

@Repository
public interface RoadWeatherStationRepository extends JpaRepository<RoadWeatherStation, Long> {

    @EntityGraph("roadWeatherStation")
    @Override
    List<RoadWeatherStation> findAll();

    List<RoadWeatherStation> findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueOrderByRoadStation_NaturalId();

    @Query(value =
           "SELECT rws.roadStation.naturalId\n" +
           "FROM RoadWeatherStation rws\n" +
           "WHERE rws.roadStation.isPublic = 1\n" +
           "  AND rws.roadStation.obsolete = 0",
           nativeQuery = false)
    List<Long> findNonObsoleteAndPublicRoadStationNaturalIds();

    RoadWeatherStation findByLotjuId(long lotjuId);
}
