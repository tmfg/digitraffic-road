package fi.livi.digitraffic.tie.metadata.dao;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {
    @Query(value =
           "SELECT s\n" +
           "FROM RoadStationSensor s\n" +
           "WHERE s.obsolete = false\n" +
           "  AND s.roadStationType = ?1\n" +
           "  AND EXISTS (\n" +
           "     FROM AllowedRoadStationSensor allowed\n" +
           "     WHERE allowed.naturalId = s.naturalId\n" +
           "       AND allowed.roadStationType = s.roadStationType\n" +
           "  )" +
           "ORDER BY s.naturalId")
    List<RoadStationSensor> findByRoadStationTypeAndObsoleteFalseAndAllowed(final RoadStationType roadStationType);

    List<RoadStationSensor> findByRoadStationType(final RoadStationType roadStationType);

    List<RoadStationSensor> findByRoadStationTypeAndLotjuIdIsNull(final RoadStationType roadStationType);

    @Query(value =
        "SELECT rs_sensors.road_station_id, LISTAGG(sensor.natural_id, ',') WITHIN GROUP (ORDER BY sensor.natural_id) AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Object[]> listRoadStationSensors();

    @Query(value =
        "SELECT LISTAGG(sensor.natural_id, ',') WITHIN GROUP (ORDER BY sensor.natural_id) AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "where rs_sensors.road_station_id = :id\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    String listRoadStationSensors(@Param("id") final long roadStationId);
}
