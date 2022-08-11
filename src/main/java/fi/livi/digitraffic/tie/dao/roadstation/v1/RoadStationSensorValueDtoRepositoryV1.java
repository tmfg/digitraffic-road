package fi.livi.digitraffic.tie.dao.roadstation.v1;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.time.Instant;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.dao.SqlRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.model.RoadStationType;

public interface RoadStationSensorValueDtoRepositoryV1 extends SqlRepository {

    String SQL_SELECT =
        "select rs.natural_id roadStationNaturalId\n" +
        "     , s.natural_id sensorNaturalId\n" +
        "     , sv.id sensorValueId\n" +
        "     , sv.value\n" +
        "     , sv.time_window_start timeWindowStart\n" +
        "     , sv.time_window_end timeWindowEnd\n" +
        "     , sv.measured as measuredTime\n" +
        "     , s.name_fi sensorNameFi\n" +
        "     , s.short_name_fi sensorShortNameFi\n" +
        "     , s.unit\n" +
        "     , svd.description_fi as sensorValueDescriptionFi\n" +
        "     , svd.description_en as sensorValueDescriptionEn\n" +
        "     , max(sv.measured) over(partition by sv.road_station_id) stationLatestMeasuredTime" +
        "     , sv.updated as updatedTime\n";

    String SQL_FROM =
        "from road_station rs\n" +
        "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
        "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
        "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
        "                                            and svd.sensor_value = sv.value\n";

    String SQL_WHERE_PUBLISHABLE =
        "where rs.publishable = true\n" +
        "  and s.publishable = true\n" +
        "  and exists (\n" +
        "     select null\n" +
        "     from allowed_road_station_sensor allowed\n" +
        "     where allowed.natural_id = s.natural_id\n" +
        "       and allowed.road_station_type = s.road_station_type\n" +
        "  )\n";

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value =
           SQL_SELECT +
           SQL_FROM +
           SQL_WHERE_PUBLISHABLE +
           "  and rs.type = :stationTypeId\n" +
           "  and sv.measured > (now() -(:timeLimitInMinutes * interval '1 MINUTE'))\n" +
           "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    List<SensorValueDtoV1> findAllPublicPublishableRoadStationSensorValues(
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("timeLimitInMinutes")
            final int timeLimitInMinutes);

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
               "  and rs.type = :#{#roadStationType.name()}\n" +
               "  and sv.updated > :afterDate\n" +
               "order by sv.updated",
                   nativeQuery = true)
    List<SensorValueDtoV1> findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
            final RoadStationType roadStationType,
            final Instant afterDate);
}
