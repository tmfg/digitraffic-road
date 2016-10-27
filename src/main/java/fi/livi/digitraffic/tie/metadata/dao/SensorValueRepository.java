package fi.livi.digitraffic.tie.metadata.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;

@Repository
public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {

    @Query(value =
           "SELECT sv\n" +
           "FROM SensorValue sv\n" +
           "WHERE sv.roadStation.naturalId = ?1\n" +
           "  AND sv.roadStationSensor.roadStationType = ?2")
    List<SensorValue> findSensorvaluesByRoadStationNaturalId(final long roadStationNaturalId, final RoadStationType roadStationType);

    List<SensorValue> findByRoadStationLotjuIdInAndRoadStationType(final List<Long> lamLotjuIds, final RoadStationType roadStationType);

    @Query(value =
           "SELECT max(sv.updated)\n" +
           "FROM SensorValue sv\n" +
           "WHERE sv.roadStation.roadStationType = ?1",
           nativeQuery = false)
    LocalDateTime getLastUpdated(final RoadStationType roadStationType);

}
