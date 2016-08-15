package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.SensorValue;

@Repository
public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {

    @Query(value =
           "SELECT sv\n" +
           "FROM SensorValue sv\n" +
           "WHERE sv.roadStation.naturalId = ?1")
    List<SensorValue> findSensorvaluesByRoadStationNaturalId(long roadStationNaturalId);
}
