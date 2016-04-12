package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.daydata.LinkData;

@Repository
public interface DayDataRepository extends org.springframework.data.repository.Repository<LinkData, Long> {
    @Query(value =
            "SELECT rownum, (m.end_timestamp - trunc(m.end_timestamp)) * 1440 as MINUTE, m.MEDIAN_TRAVEL_TIME, m.AVERAGE_SPEED, fc.CODE as FC, l.NATURAL_ID as LINK_ID\n"
                    + "FROM JOURNEYTIME_MEDIAN m, LINK l, FLUENCY_CLASS FC\n"
                    + "WHERE m.END_TIMESTAMP >= (trunc(SYSDATE) -1)\n"
                    + "AND m.END_TIMESTAMP < (trunc(SYSDATE))\n"
                    + "AND m.LINK_ID = l.ID\n"
                    + "AND l.OBSOLETE = 0\n"
                    + "AND m.RATIO_TO_FREE_FLOW_SPEED >= fc.LOWER_LIMIT and m.RATIO_TO_FREE_FLOW_SPEED < nvl(FC.UPPER_LIMIT, 10)\n"
                    + "ORDER BY l.NATURAL_ID, m.END_TIMESTAMP",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LinkData> listAllMedianTravelTimes();
}
