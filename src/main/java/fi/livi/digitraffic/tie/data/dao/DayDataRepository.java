package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.daydata.LinkDataDto;

@Repository
public interface DayDataRepository extends org.springframework.data.repository.Repository<LinkDataDto, Long> {
    @Query(value =
            "SELECT ROWNUM\n" +
            "     , (M.END_TIMESTAMP - TRUNC(M.END_TIMESTAMP)) * 1440 AS MINUTE\n" +
            "     , M.MEDIAN_TRAVEL_TIME\n" +
            "     , M.AVERAGE_SPEED, FC.CODE AS FC\n" +
            "     , L.NATURAL_ID AS LINK_ID\n" +
            "FROM JOURNEYTIME_MEDIAN M\n" +
            "INNER JOIN LINK L ON M.LINK_ID = L.ID\n" +
            "INNER JOIN FLUENCY_CLASS FC ON M.RATIO_TO_FREE_FLOW_SPEED >= FC.LOWER_LIMIT\n" +
            "                           AND M.RATIO_TO_FREE_FLOW_SPEED < NVL(FC.UPPER_LIMIT, 10)\n" +
            "WHERE M.END_TIMESTAMP >= (TRUNC(SYSDATE) -1)\n" +
            "  AND M.END_TIMESTAMP < (TRUNC(SYSDATE))\n" +
            "  AND L.OBSOLETE = 0\n" +
            "ORDER BY L.NATURAL_ID, M.END_TIMESTAMP",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LinkDataDto> listAllMedianTravelTimes();
}
