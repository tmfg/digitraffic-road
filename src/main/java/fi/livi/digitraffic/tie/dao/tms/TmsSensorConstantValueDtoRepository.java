package fi.livi.digitraffic.tie.dao.tms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDto;
import jakarta.persistence.QueryHint;

public interface TmsSensorConstantValueDtoRepository extends JpaRepository<TmsSensorConstantValueDto, Long> {

    @Query(value =
               "SELECT scv.LOTJU_ID, sc.NAME, scv.VALUE, scv.VALID_FROM, scv.VALID_TO, rs.natural_id as road_station_id,\n" +
               "       scv.modified \n" +
               "FROM TMS_SENSOR_CONSTANT sc\n" +
               "INNER JOIN TMS_SENSOR_CONSTANT_VALUE scv on scv.SENSOR_CONSTANT_LOTJU_ID = sc.LOTJU_ID\n" +
               "INNER JOIN ROAD_STATION rs on rs.id = sc.ROAD_STATION_ID\n" +
               "WHERE EXISTS(SELECT null FROM ALLOWED_TMS_SENSOR_CONSTANT a WHERE a.NAME = sc.NAME)\n" +
               "  AND sc.OBSOLETE_DATE is null\n" +
               "  AND scv.OBSOLETE_DATE is null\n" +
               "  AND rs.publishable = true\n" +
               "ORDER BY ROAD_STATION_ID, sc.NAME, scv.valid_from",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<TmsSensorConstantValueDto> findAllPublishableSensorConstantValues();

    @Query(value =
               "SELECT scv.LOTJU_ID, sc.NAME, scv.VALUE, scv.VALID_FROM, scv.VALID_TO, rs.natural_id as road_station_id,\n" +
               "       scv.modified \n" +
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
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<TmsSensorConstantValueDto> findPublishableSensorConstantValueForStation(final long roadStationNaturalId);
}
