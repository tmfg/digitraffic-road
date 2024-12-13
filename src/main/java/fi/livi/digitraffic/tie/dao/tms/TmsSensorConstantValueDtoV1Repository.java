package fi.livi.digitraffic.tie.dao.tms;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDtoV1;
import jakarta.persistence.QueryHint;

public interface TmsSensorConstantValueDtoV1Repository extends JpaRepository<TmsSensorConstantValueDtoV1, Long> {

    String SELECT_LIST =
        "SELECT scv.LOTJU_ID, sc.lotju_id as constant_lotju_id, sc.NAME, scv.VALUE, scv.VALID_FROM, scv.VALID_TO, rs.natural_id as road_station_id, GREATEST(scv.modified, sc.modified) as modified\n";

    @Query(value =
               SELECT_LIST +
                   "FROM TMS_SENSOR_CONSTANT sc\n" +
                   "INNER JOIN TMS_SENSOR_CONSTANT_VALUE scv on scv.SENSOR_CONSTANT_LOTJU_ID = sc.LOTJU_ID\n" +
                   "INNER JOIN ROAD_STATION rs on rs.id = sc.ROAD_STATION_ID\n" +
                   "WHERE EXISTS(SELECT null FROM ALLOWED_TMS_SENSOR_CONSTANT a WHERE a.NAME = sc.NAME)\n" +
                   "  AND sc.OBSOLETE_DATE is null\n" +
                   "  AND scv.OBSOLETE_DATE is null\n" +
                   "  AND rs.publishable = true\n" +
                   "ORDER BY ROAD_STATION_ID, sc.NAME, scv.valid_from",
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize",
                           value = "1000"))
    List<TmsSensorConstantValueDtoV1> findAllPublishableSensorConstantValues();

    @Query(value =
               SELECT_LIST +
                   "FROM TMS_SENSOR_CONSTANT sc\n" +
                   "INNER JOIN TMS_SENSOR_CONSTANT_VALUE scv on scv.SENSOR_CONSTANT_LOTJU_ID = sc.LOTJU_ID\n" +
                   "INNER JOIN ROAD_STATION rs on rs.id = sc.ROAD_STATION_ID\n" +
                   "WHERE EXISTS(SELECT null FROM ALLOWED_TMS_SENSOR_CONSTANT a WHERE a.NAME = sc.NAME)\n" +
                   "  AND sc.OBSOLETE_DATE is null\n" +
                   "  AND scv.OBSOLETE_DATE is null\n" +
                   "  AND rs.PUBLISHABLE = true\n" +
                   "  AND rs.NATURAL_ID = :roadStationNaturalId\n" +
                   "ORDER BY sc.NAME, scv.valid_from",
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize",
                           value = "1000"))
    List<TmsSensorConstantValueDtoV1> findPublishableSensorConstantValueForStation(final long roadStationNaturalId);

    @Query(value =
               SELECT_LIST +
                   "FROM TMS_SENSOR_CONSTANT_VALUE scv\n" +
                   "INNER JOIN TMS_SENSOR_CONSTANT sc on sc.LOTJU_ID = scv.SENSOR_CONSTANT_LOTJU_ID\n" +
                   "INNER JOIN ROAD_STATION rs on rs.id = sc.ROAD_STATION_ID\n" +
                   "WHERE scv.lotju_id = :sensorConstantValueLotjuId\n" +
                   "  AND rs.lotju_id = :stationLotjuId\n" +
                   "ORDER BY sc.NAME, scv.valid_from",
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize",
                           value = "1000"))
    TmsSensorConstantValueDtoV1 getStationSensorConstantValue(final long stationLotjuId, final long sensorConstantValueLotjuId);

    @Query(value = """
            SELECT GREATEST(tsc.modified, tscv.modified) as data_last_updated
            FROM tms_sensor_constant tsc
            INNER JOIN tms_sensor_constant_value tscv
            ON tscv.sensor_constant_lotju_id = tsc.lotju_id
            ORDER BY data_last_updated DESC
            LIMIT 1
        """,
           nativeQuery = true)
    Instant getTmsSensorConstantsLastUpdated();
}
