package fi.livi.digitraffic.tie.dao.v1;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;

@Repository
public interface TmsFreeFlowSpeedRepository extends JpaRepository<TmsFreeFlowSpeedDto, Long> {

    String SELECT_TMS_AND_RS_NATURAL_IDS_AND_FREE_FLOW_SPEEDS =
        "WITH free_flow_speed AS (\n" +
        "    SELECT sc.ROAD_STATION_ID, sc.NAME, scv.VALID_FROM, scv.VALID_TO, scv.VALUE\n" +
        "    FROM (SELECT (EXTRACT(DAY FROM now()) + (EXTRACT(MONTH FROM now())*100)) as time FROM now()) AS time_now\n" +
        "       , TMS_SENSOR_CONSTANT sc INNER JOIN TMS_SENSOR_CONSTANT_VALUE scv ON scv.SENSOR_CONSTANT_LOTJU_ID = sc.LOTJU_ID\n" +
        "    WHERE sc.NAME LIKE 'VVAPAAS%'\n" +
        "      AND sc.OBSOLETE_DATE IS NULL\n" +
        "      AND ((scv.VALID_FROM <= time_now.time AND scv.VALID_TO >= time_now.time)\n" +
        "        OR (scv.VALID_FROM >= time_now.time AND scv.VALID_TO >= time_now.time AND scv.VALID_FROM >= scv.VALID_TO)\n" +
        "        OR (scv.VALID_FROM <= time_now.time AND scv.VALID_TO <= time_now.time AND scv.VALID_FROM >= scv.VALID_TO))\n" +
        ")\n" +
        "SELECT rs.natural_id AS road_station_natural_id\n" +
        "     , tms.natural_id AS tms_natural_id\n" +
        "     , COALESCE(free_flow_speed1.value, -1) AS free_flow_speed1\n" +
        "     , COALESCE(free_flow_speed2.value, -1) AS free_flow_speed2\n" +
        "FROM tms_station tms\n" +
        "INNER JOIN road_station rs ON rs.id = tms.road_station_id\n" +
        "LEFT OUTER JOIN free_flow_speed AS free_flow_speed1 ON free_flow_speed1.name = 'VVAPAAS1' AND free_flow_speed1.road_station_id = tms.road_station_id\n" +
        "LEFT OUTER JOIN free_flow_speed AS free_flow_speed2 ON free_flow_speed2.name = 'VVAPAAS2' AND free_flow_speed2.road_station_id = tms.road_station_id\n" +
        "WHERE rs.is_public = true\n";

    @Query(value = SELECT_TMS_AND_RS_NATURAL_IDS_AND_FREE_FLOW_SPEEDS + "ORDER BY rs.natural_id", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<TmsFreeFlowSpeedDto> findAllPublicTmsFreeFlowSpeeds();

    @Query(value = SELECT_TMS_AND_RS_NATURAL_IDS_AND_FREE_FLOW_SPEEDS + "AND RS.NATURAL_ID = :roadStationNaturalId", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    TmsFreeFlowSpeedDto getTmsFreeFlowSpeedsByRoadStationNaturalId(final long roadStationNaturalId);
}
