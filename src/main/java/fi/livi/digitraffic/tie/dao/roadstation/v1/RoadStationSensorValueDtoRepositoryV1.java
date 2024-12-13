package fi.livi.digitraffic.tie.dao.roadstation.v1;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.dao.SqlRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import jakarta.persistence.QueryHint;

public interface RoadStationSensorValueDtoRepositoryV1 extends SqlRepository {

    String SQL_SELECT = """
        select rs.natural_id roadStationNaturalId
             , s.natural_id sensorNaturalId
             , sv.id sensorValueId
             , sv.value
             , sv.time_window_start timeWindowStart
             , sv.time_window_end timeWindowEnd
             , sv.measured as measuredTime
             , s.name_fi sensorNameFi
             , s.short_name_fi sensorShortNameFi
             , s.unit
             , svd.description_fi as sensorValueDescriptionFi
             , svd.description_en as sensorValueDescriptionEn
             , max(sv.measured) over(partition by sv.road_station_id) stationLatestMeasuredTime
             , max(sv.modified) over(partition by sv.road_station_id) stationLatestModifiedTime
             , sv.modified
             , sv.reliability
        """;

    String SQL_FROM = """
        from road_station rs
        inner join sensor_value sv on sv.road_station_id = rs.id
        inner join road_station_sensor s on sv.road_station_sensor_id = s.id
        left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id
                                                    and svd.sensor_value = sv.value
        """;

    String SQL_WHERE_PUBLISHABLE = """
        where rs.publishable = true
          and s.publishable = true
        """;

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value =
           SQL_SELECT +
           SQL_FROM +
           SQL_WHERE_PUBLISHABLE +
           "  and rs.road_station_type = :#{#roadStationType.name()}\n" +
           "  and sv.measured > (now() -(:timeLimitInMinutes * interval '1 MINUTE'))\n" +
           "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    List<SensorValueDtoV1> findAllPublicPublishableRoadStationSensorValues(
            final RoadStationType roadStationType,
            final int timeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = SQL_SELECT +
            SQL_FROM +
            SQL_WHERE_PUBLISHABLE +
            " AND rs.road_station_type = :#{#roadStationType.name()}\n" +
            " AND sv.measured > (now() -(:measuredTimeLimitInMinutes * interval '1 MINUTE'))\n" +
            " AND s.name_fi IN (:sensorNames) ", nativeQuery = true)
    List<SensorValueDtoV1> findAllPublicPublishableRoadStationSensorValues(
            final RoadStationType roadStationType,
            final int measuredTimeLimitInMinutes,
            final Collection<String> sensorNames);


    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value =
           SQL_SELECT +
           SQL_FROM +
           SQL_WHERE_PUBLISHABLE +
           "  and rs.road_station_type = :#{#roadStationType.name()}\n" +
           "  and rs.natural_id = :roadStationNaturalId\n" +
           "  and sv.measured > (now() -(:timeLimitInMinutes * interval '1 MINUTE'))\n" +
           "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    List<SensorValueDtoV1> findAllPublicPublishableRoadStationSensorValues(
            final long roadStationNaturalId,
            final RoadStationType roadStationType,
            final int timeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value =
               SQL_SELECT +
               SQL_FROM +
               SQL_WHERE_PUBLISHABLE +
               "  and rs.road_station_type = :#{#roadStationType.name()}\n" +
               "  and sv.modified > :afterDate\n" +
               "order by sv.modified",
                   nativeQuery = true)
    List<SensorValueDtoV1> findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
            final RoadStationType roadStationType,
            final Instant afterDate);
}
