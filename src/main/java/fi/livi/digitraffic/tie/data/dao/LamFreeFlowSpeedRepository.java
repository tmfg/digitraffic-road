package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.lam.LamFreeFlowSpeedDto;

@Repository
public interface LamFreeFlowSpeedRepository extends JpaRepository<LamFreeFlowSpeedDto, Long> {

    @Query(value =
            "SELECT LS.NATURAL_ID AS LAM_ID\n" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "            THEN LS.SUMMER_FREE_FLOW_SPEED_1\n" +
            "            ELSE LS.WINTER_FREE_FLOW_SPEED_1\n" +
            "       END AS FREE_FLOW_SPEED1\n" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "            THEN LS.SUMMER_FREE_FLOW_SPEED_2\n" +
            "            ELSE LS.WINTER_FREE_FLOW_SPEED_2\n" +
            "        END AS FREE_FLOW_SPEED2\n" +
            "FROM LAM_STATION LS\n" +
            "INNER JOIN ROAD_DISTRICT RD ON LS.ROAD_DISTRICT_ID = RD.ID\n" +
            "WHERE LS.OBSOLETE = 0",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LamFreeFlowSpeedDto> listAllLamFreeFlowSpeeds();
}
