package fi.livi.digitraffic.tie.metadata.dao;

import java.time.Instant;
import java.util.List;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;

@Repository
public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {
    @EntityGraph(attributePaths = {"roadStation", "roadStationSensor"})
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<SensorValue> findByRoadStationObsoleteDateIsNullAndRoadStationSensorObsoleteDateIsNullAndRoadStationLotjuIdInAndRoadStationType(final List<Long> tmsLotjuIds, final RoadStationType roadStationType);

    @Query(
           "SELECT max(sv.updated)\n" +
           "FROM SensorValue sv\n" +
           "WHERE sv.roadStation.roadStationType = ?1")
    Instant getLastUpdated(final RoadStationType roadStationType);
}
