package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.dto.StationSensor;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, Long> {
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByLotjuIdIsNull();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByRoadStationIsNull();

    @Query(value =
        "SELECT rs_sensors.road_station_id, LISTAGG(sensor.natural_id, ',') WITHIN GROUP (ORDER BY sensor.natural_id) AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id\n" +
            "where sensor.obsolete_date is null\n" +
            "and sensor.road_station_type = 'WEATHER_STATION'\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<StationSensor> listWeatherStationSensors();
}
