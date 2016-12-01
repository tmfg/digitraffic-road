package fi.livi.digitraffic.tie.data.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.trafficfluency.LatestMedianDataDto;

@Repository
public interface TrafficFluencyRepository extends JpaRepository<LatestMedianDataDto, Long> {

    @Query(value =
           "SELECT max(M.END_TIMESTAMP) AS MEASURED_TIME\n" +
           "FROM LATEST_JOURNEYTIME_MEDIAN M\n" +
           "INNER JOIN LINK L ON M.LINK_ID = L.ID\n" +
           "WHERE L.OBSOLETE = 0",
           nativeQuery = true)
    LocalDateTime getLatestMeasurementTime();

    @Query(value =
            "SELECT M.ID\n" +
            "     , M.END_TIMESTAMP AS MEASURED_TIME\n" +
            "     , M.MEDIAN_TRAVEL_TIME AS MEDIAN_JOURNEY_TIME\n" +
            "     , M.AVERAGE_SPEED AS MEDIAN_SPEED\n" +
            "     , M.RATIO_TO_FREE_FLOW_SPEED\n" +
            "     , M.NOBS\n" +
            "     , L.NATURAL_ID AS LINK_NATURAL_ID\n" +
            "FROM LATEST_JOURNEYTIME_MEDIAN M\n" +
            "INNER JOIN LINK L ON M.LINK_ID = L.ID\n" +
            "WHERE L.OBSOLETE = 0 \n" +
            "ORDER BY L.NATURAL_ID",
            nativeQuery = true)
    List<LatestMedianDataDto> findLatestMediansForNonObsoleteLinks();

    @Query(value =
            "SELECT M.ID\n" +
            "     , M.END_TIMESTAMP AS MEASURED_TIME\n" +
            "     , M.MEDIAN_TRAVEL_TIME AS MEDIAN_JOURNEY_TIME\n" +
            "     , M.AVERAGE_SPEED AS MEDIAN_SPEED\n" +
            "     , M.RATIO_TO_FREE_FLOW_SPEED\n" +
            "     , M.NOBS\n" +
            "     , L.NATURAL_ID AS LINK_NATURAL_ID\n" +
            "FROM LATEST_JOURNEYTIME_MEDIAN M\n" +
            "INNER JOIN LINK L ON M.LINK_ID = L.ID\n" +
            "WHERE L.OBSOLETE = 0 \n" +
            "  AND L.NATURAL_ID = ?1\n" +
            "ORDER BY L.NATURAL_ID",
            nativeQuery = true)
    List<LatestMedianDataDto> findLatestMediansForLink(final long linkId);

}
