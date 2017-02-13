package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.LinkFastLaneDto;

@Repository
public class LinkFastLaneDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Return link id, length, free flow speeds, and which (summer/winter)
     * season it is. Returns only non-obsolete links.
     *
     * @return Map (link natural id -> link data)
     */
    public Map<Long, LinkFastLaneDto> findNonObsoleteLinks() {
        return findLinks("AND l.obsolete = 0");
    }

    /**
     * Return link id, length, free flow speeds, and which (summer/winter)
     * season it is.
     *
     * @return Map (link natural id -> link data)
     */
    public Map<Long, LinkFastLaneDto> findLinks() {
        return findLinks("");
    }

    private Map<Long, LinkFastLaneDto> findLinks(final String obsoleteCriteria) {
        List<LinkFastLaneDto> links = jdbcTemplate
                .query("SELECT l.id, l.natural_id, l.summer_free_flow_speed, l.winter_free_flow_speed, l.length, d.speed_limit_season " +
                       "FROM link l, road_district d " +
                       "WHERE l.road_district_id = d.id " +
                       obsoleteCriteria,
                       (rs, rowNum) -> new LinkFastLaneDto(rs.getLong("natural_id"),
                                                           rs.getLong("id"),
                                                           rs.getLong("length"),
                                                           rs.getDouble("summer_free_flow_speed"),
                                                           rs.getDouble("winter_free_flow_speed"),
                                                           getSpeedLimitSeason(rs.getInt("speed_limit_season"))));

        Map<Long, LinkFastLaneDto> map = links.stream().collect(Collectors.toMap(LinkFastLaneDto::getNaturalId, Function.identity()));
        return map;
    }

    private static boolean getSpeedLimitSeason(final int season) {
        return season == 1 ? false : true;
    }
}