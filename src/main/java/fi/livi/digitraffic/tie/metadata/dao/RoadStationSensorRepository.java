package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {
    @Query("SELECT s\n" +
           "FROM RoadStationSensor s\n" +
           "WHERE s.obsolete = false\n" +
           "  AND s.roadStationType = ?1\n" +
           "  AND EXISTS (\n" +
           "     FROM AllowedRoadStationSensor allowed\n" +
           "     WHERE allowed.naturalId = s.naturalId\n" +
           "       AND allowed.roadStationType = s.roadStationType\n" +
           "  )" +
           "ORDER BY s.naturalId")
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findByRoadStationTypeAndObsoleteFalseAndAllowed(final RoadStationType roadStationType);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findByRoadStationType(final RoadStationType roadStationType);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findByRoadStationTypeAndLotjuIdIsNull(final RoadStationType roadStationType);

    @Query(value =
        "SELECT rs_sensors.road_station_id, LISTAGG(sensor.natural_id, ',') WITHIN GROUP (ORDER BY sensor.natural_id) AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id\n" +
            "where sensor.obsolete_date is null\n" +
            "and sensor.road_station_type = 'TMS_STATION'\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Object[]> listRoadStationSensors();

    @Query(value =
        "SELECT LISTAGG(sensor.natural_id, ',') WITHIN GROUP (ORDER BY sensor.natural_id) AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id\n" +
            "where rs_sensors.road_station_id = :id\n" +
            "and sensor.obsolete_date is null\n" +
            "and sensor.road_station_type = 'TMS_STATION'\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    String listRoadStationSensors(@Param("id") final long roadStationId);
}
