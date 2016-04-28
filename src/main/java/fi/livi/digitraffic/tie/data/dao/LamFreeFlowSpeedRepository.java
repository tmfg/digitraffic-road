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
            "select ls.natural_id as lam_id\n"
          + "     , case when speed_limit_season = 1\n"
          + "            then summer_free_flow_speed_1\n"
          + "            else winter_free_flow_speed_1\n"
          + "       end free_flow_speed1\n"
          + "     , case when speed_limit_season = 1\n"
          + "            then summer_free_flow_speed_2\n"
          + "            else winter_free_flow_speed_2\n"
          + "        end free_flow_speed2\n"
          + "from lam_station ls\n"
          + "inner join road_district rd on ls.road_district_id = rd.id\n"
          + "where ls.obsolete = 0",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LamFreeFlowSpeed> listAllLamFreeFlowSpeeds();
}
