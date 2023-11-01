package fi.livi.digitraffic.tie.dao.roadstation;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.dto.v1.StationSensors;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import jakarta.persistence.QueryHint;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {

    @Query("""
        SELECT s
        FROM RoadStationSensor s
        WHERE s.publishable = true
          AND s.roadStationType = ?1
          AND EXISTS (
             FROM AllowedRoadStationSensor allowed
             WHERE allowed.naturalId = s.naturalId
               AND allowed.roadStationType = s.roadStationType
          )
        ORDER BY s.naturalId""")
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findByRoadStationTypeAndPublishable(final RoadStationType roadStationType);

    @EntityGraph(attributePaths = {"sensorValueDescriptions"})
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findDistinctByRoadStationType(final RoadStationType roadStationType);

    @EntityGraph(attributePaths = {"sensorValueDescriptions"})
    RoadStationSensor findByRoadStationTypeAndLotjuId(final RoadStationType roadStationType, final Long sensorLotjuId);

    @Query(value = """
        SELECT rs_sensors.road_station_id roadStationId, string_agg(cast(sensor.natural_id as varchar), ',' order by sensor.natural_id) AS sensors
        FROM road_station_sensor sensor
        inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id
        inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id
        where sensor.publishable = true
          and sensor.road_station_type = :#{#roadStationType.name()}
        GROUP BY rs_sensors.road_station_id
order by rs_sensors.road_station_id""", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<StationSensors> listStationPublishableSensorsByType(@Param("roadStationType") final RoadStationType roadStationType);

    @Query(value = """
        SELECT rs_sensors.road_station_id roadStationId, string_agg(cast(sensor.natural_id as varchar), ',' order by sensor.natural_id) AS sensors
        FROM road_station_sensor sensor
        inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id
        inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id
        where rs_sensors.road_station_id = :id
          and sensor.publishable = true
          and sensor.road_station_type = :#{#roadStationType.name()}
        GROUP BY rs_sensors.road_station_id
        order by rs_sensors.road_station_id""", nativeQuery = true)
    List<StationSensors> getRoadStationPublishableSensorsNaturalIdsByStationIdAndType(@Param("id") final long roadStationId,
                                                                                      @Param("roadStationType") final RoadStationType roadStationType);

    @Query(value = """
        SELECT sensor.natural_id
        FROM road_station_sensor sensor
        inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id
        inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id
        where rs_sensors.road_station_id = :id
          and sensor.publishable = true
          and sensor.road_station_type = :#{#roadStationType.name()}
        order by sensor.natural_id""", nativeQuery = true)
    List<Long> findRoadStationPublishableSensorsNaturalIdsByStationIdAndType(@Param("id") final long roadStationId,
                                                                             @Param("roadStationType") final RoadStationType roadStationType);

    @Modifying(clearAutomatically = true)
    @Query(value =
            "DELETE FROM ROAD_STATION_SENSORS\n" +
            "WHERE ROAD_STATION_ID = :roadStationId\n" +
            "AND ROAD_STATION_SENSOR_ID NOT IN (\n" +
            "    SELECT SENSOR.ID\n" +
            "    FROM ROAD_STATION_SENSOR SENSOR\n" +
            "    WHERE SENSOR.ROAD_STATION_TYPE = :#{#roadStationType.name()}\n" +
            "      AND SENSOR.LOTJU_ID IN (:sensorsLotjuIds)\n" +
            ")",
           nativeQuery = true)
    int deleteNonExistingSensors(@Param("roadStationType") final RoadStationType roadStationType,
                                 @Param("roadStationId") final Long roadStationId,
                                 @Param("sensorsLotjuIds") final List<Long> sensorsLotjuIds);

    @Modifying(clearAutomatically = true)
    @Query(value =
            "DELETE FROM ROAD_STATION_SENSORS\n" +
            "WHERE ROAD_STATION_ID = :roadStationId",
           nativeQuery = true)
    int deleteRoadStationsSensors(@Param("roadStationId") final Long roadStationId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
        INSERT INTO ROAD_STATION_SENSORS (ROAD_STATION_ID, ROAD_STATION_SENSOR_ID)
          SELECT RS.ID AS ROAD_STATION_ID
               , SENSOR.ID AS ROAD_STATION_SENSOR_ID
          FROM ROAD_STATION_SENSOR SENSOR, ROAD_STATION RS
          WHERE SENSOR.ROAD_STATION_TYPE = :#{#roadStationType.name()}
            AND RS.ROAD_STATION_TYPE = SENSOR.ROAD_STATION_TYPE
            AND SENSOR.LOTJU_ID IN (:sensorsLotjuIds)
            AND RS.ID = :roadStationId
            AND NOT EXISTS(
              SELECT NULL
              FROM ROAD_STATION_SENSORS RSS
              WHERE RSS.ROAD_STATION_ID = RS.ID
                AND RSS.ROAD_STATION_SENSOR_ID = SENSOR.ID
          )""", nativeQuery = true)
    int insertNonExistingSensors(@Param("roadStationType") final RoadStationType roadStationType,
                                 @Param("roadStationId") final Long roadStationId,
                                 @Param("sensorsLotjuIds") final List<Long> sensorsLotjuIds);
}