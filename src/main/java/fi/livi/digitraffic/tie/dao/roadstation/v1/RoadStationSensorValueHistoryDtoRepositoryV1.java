package fi.livi.digitraffic.tie.dao.roadstation.v1;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.dao.SqlRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import jakarta.persistence.QueryHint;

public interface RoadStationSensorValueHistoryDtoRepositoryV1 extends SqlRepository {

    String SQL_SELECT = """
        select rs.natural_id roadStationNaturalId
             , s.natural_id sensorNaturalId
             , sv.id sensorValueId
             , sv.value
             , sv.measured as measuredTime
             , max(sv.measured) over(partition by sv.road_station_id) stationLatestMeasuredTime
             , max(sv.modified) over(partition by sv.road_station_id) stationLatestModifiedTime
             , sv.modified
             , sv.reliability
        """;

    String SQL_FROM = """
        from road_station rs
        inner join sensor_value_history sv on sv.road_station_id = rs.id
        inner join road_station_sensor s on sv.road_station_sensor_id = s.id
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
                   "  and rs.type = :#{#roadStationType.name()}\n" +
                   "  and rs.natural_id = :roadStationNaturalId\n" +
                   "  and s.natural_id = :sensorNaturalId\n" +
                   "  and :actualFrom < sv.measured\n" +
                   "  and sv.measured <= :actualTo\n" +
                   "order by s.natural_id asc, sv.measured asc",
           nativeQuery = true)
    List<SensorValueHistoryDtoV1> findAllPublicPublishableRoadStationSensorValues(
            final long roadStationNaturalId,
            final long sensorNaturalId,
            final RoadStationType roadStationType,
            final Instant actualFrom,
            final Instant actualTo);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value =
           SQL_SELECT +
           SQL_FROM +
           SQL_WHERE_PUBLISHABLE +
           "  and rs.type = :#{#roadStationType.name()}\n" +
           "  and rs.natural_id = :roadStationNaturalId\n" +
           "  and :actualFrom < sv.measured\n" +
           "  and sv.measured <= :actualTo\n" +
           "order by s.natural_id asc, sv.measured asc",
           nativeQuery = true)
    List<SensorValueHistoryDtoV1> findAllPublicPublishableRoadStationSensorValues(
            final long roadStationNaturalId,
            final RoadStationType roadStationType,
            final Instant actualFrom,
            final Instant actualTo);

    /**
     *
     * @param roadStationType
     * @param from inclusive start time
     * @param to exclusive end time
     * @return
     */
    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value =
                   SQL_SELECT +
                           SQL_FROM +
                           SQL_WHERE_PUBLISHABLE +
                           "  and rs.type = :#{#roadStationType.name()}\n" +
                           "  and :from <= sv.measured\n" +
                           "  and sv.measured < :to\n" +
                           "order by sv.measured",
           nativeQuery = true)
    List<SensorValueHistoryDtoV1> findAllPublicPublishableRoadStationSensorValuesBetween(
            final RoadStationType roadStationType,
            final Instant from, final Instant to);
}
