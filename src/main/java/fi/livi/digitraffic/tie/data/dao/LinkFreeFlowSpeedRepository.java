package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.freeflowspeed.LinkFreeFlowSpeedDto;

@Repository
public interface LinkFreeFlowSpeedRepository extends JpaRepository<LinkFreeFlowSpeedDto, Long> {
    @Query(value =
            "SELECT L.NATURAL_ID AS LINK_NO" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "           THEN L.SUMMER_FREE_FLOW_SPEED\n" +
            "           ELSE L.WINTER_FREE_FLOW_SPEED\n" +
            "       END AS FREE_FLOW_SPEED\n" +
            "FROM LINK L\n" +
            "INNER JOIN ROAD_DISTRICT RD ON L.ROAD_DISTRICT_ID = RD.ID\n" +
            "WHERE L.OBSOLETE = 0",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LinkFreeFlowSpeedDto> listAllLinkFreeFlowSpeeds();

    @Query(value =
            "SELECT L.NATURAL_ID AS LINK_NO" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "           THEN L.SUMMER_FREE_FLOW_SPEED\n" +
            "           ELSE L.WINTER_FREE_FLOW_SPEED\n" +
            "       END AS FREE_FLOW_SPEED\n" +
            "FROM LINK L\n" +
            "INNER JOIN ROAD_DISTRICT RD ON L.ROAD_DISTRICT_ID = RD.ID\n" +
            "WHERE L.OBSOLETE = 0\n" +
            "  AND L.NATURAL_ID = ?1",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LinkFreeFlowSpeedDto> listAllLinkFreeFlowSpeeds(final long linkId);

    @Query(value =
           "SELECT CASE WHEN count(*) > 0 THEN 1 ELSE 0 END\n" +
           "FROM LINK L\n" +
           "WHERE L.NATURAL_ID = ?1\n" +
           "  AND L.OBSOLETE = 0",
           nativeQuery = true)
    int linkExists(final long linkNaturalId);
}
