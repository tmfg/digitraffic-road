package fi.livi.digitraffic.tie.dao.roadstation;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import jakarta.persistence.QueryHint;

public interface RoadStationSensorValueDtoRepository extends JpaRepository<SensorValueDto, Long> {

    String SQL_SELECT_FROM_AND_PUBLISHABLE = """
        SELECT rs.natural_id road_station_natural_id
             , s.natural_id sensor_natural_id
             , sv.id sensor_value_id
             , sv.value sensor_value
             , sv.time_window_start
             , sv.time_window_end
             , sv.measured as measured_time
             , s.name sensor_name_old
             , s.name_fi sensor_name_fi
             , s.short_name_fi sensor_short_name_fi
             , s.unit sensor_unit
             , svd.description_fi as sensor_value_description_fi
             , svd.description_en as sensor_value_description_en
             , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time
             , max(sv.modified) over(partition by sv.road_station_id) station_latest_modified_time
             , sv.modified
        FROM road_station rs
        INNER JOIN sensor_value sv ON sv.road_station_id = rs.id
        INNER JOIN road_station_sensor s ON sv.road_station_sensor_id = s.id
        LEFT OUTER JOIN sensor_value_description svd ON svd.sensor_id = sv.road_station_sensor_id
                                                    AND svd.sensor_value = sv.value
        WHERE rs.road_station_type = :#{#roadStationType.name()}
         AND rs.publishable = true
         AND s.publishable = true""";

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = SQL_SELECT_FROM_AND_PUBLISHABLE +
          " AND sv.measured > (now() -(:measuredTimeLimitInMinutes * interval '1 MINUTE'))", nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValues(
            final RoadStationType roadStationType,
            final int measuredTimeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = SQL_SELECT_FROM_AND_PUBLISHABLE +
        " AND sv.modified > :afterDate \n" +
        "ORDER BY sv.modified", nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
            final RoadStationType roadStationType,
            final Instant afterDate);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = SQL_SELECT_FROM_AND_PUBLISHABLE +
            " AND sv.measured > (now() -(:measuredTimeLimitInMinutes * interval '1 MINUTE'))\n" +
            " AND s.name_fi IN (:sensorNames) ", nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValues(
            final RoadStationType roadStationType,
            final int measuredTimeLimitInMinutes,
            final Collection<String> sensorNames);
}
