package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.LamFreeFlowSpeed;

@Repository
public interface LamFreeFlowSpeedRepository extends JpaRepository<LamFreeFlowSpeed, Long> {
    @Query(value =
            "select ls.natural_id as lam_id, case when speed_limit_season = 1 then summer_free_flow_speed_1 else winter_free_flow_speed_1" +
                    " end free_flow_speed1, case when speed_limit_season = 1 then summer_free_flow_speed_2 else winter_free_flow_speed_2" +
                    " end free_flow_speed2\n" +
                    "from lam_station ls, road_district rd\n" +
                    "where ls.obsolete = 0\n" +
                    "and ls.road_district_id = rd.id",
            nativeQuery = true)

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LamFreeFlowSpeed> listAllLamFreeFlowSpeeds();
}
