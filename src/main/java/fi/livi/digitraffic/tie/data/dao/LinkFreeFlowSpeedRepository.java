package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.LinkFreeFlowSpeed;

@Repository
public interface LinkFreeFlowSpeedRepository extends JpaRepository<LinkFreeFlowSpeed, Long> {
    @Query(value =
            "select l.natural_id as link_no, case when speed_limit_season = 1 then summer_free_flow_speed else winter_free_flow_speed end" +
                    " as free_flow_speed\n" +
                    "from link l, road_district rd\n" +
                    "where l.obsolete = 0\n" +
                    "and l.ROAD_DISTRICT_ID = rd.id",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LinkFreeFlowSpeed> listAllLinkFreeFlowSpeeds();
}
