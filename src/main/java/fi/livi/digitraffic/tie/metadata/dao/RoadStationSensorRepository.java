package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {

    List<RoadStationSensor> findByRoadStationTypeAndObsoleteFalse(RoadStationType roadStationType);

    List<RoadStationSensor> findByRoadStationType(RoadStationType roadStationType);

    @Query(value =
           "SELECT s.naturalId\n" +
           "FROM RoadStationSensor s\n" +
           "WHERE s.obsolete = false\n" +
           "  AND s.roadStationType = :roadStationType")
    List<Long> findAllNonObsoleteRoadStationSensorNaturalIdsByRoadStationType(@Param("roadStationType")
                                                                              RoadStationType roadStationType);
}
