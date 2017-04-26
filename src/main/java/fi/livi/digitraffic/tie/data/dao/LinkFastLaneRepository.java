package fi.livi.digitraffic.tie.data.dao;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.service.traveltime.dto.LinkFastLaneDto;
import fi.livi.digitraffic.tie.metadata.model.SpeedLimitSeason;

@Repository
public class LinkFastLaneRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Return link id, length, free flow speeds, and which (summer/winter)
     * season it is. Returns only non-obsolete links.
     *
     * @return Map (link natural id -> link data)
     */
    public Map<Long, LinkFastLaneDto> findNonObsoleteLinks() {
        List<LinkFastLaneDto> links = jdbcTemplate
                .query("SELECT l.id, l.natural_id, l.summer_free_flow_speed, l.winter_free_flow_speed, l.length, d.speed_limit_season " +
                       "FROM link l, road_district d " +
                       "WHERE l.road_district_id = d.id " +
                       "AND l.obsolete = 0",
                       (rs, rowNum) -> new LinkFastLaneDto(rs.getLong("natural_id"),
                                                           rs.getLong("id"),
                                                           rs.getLong("length"),
                                                           rs.getDouble("summer_free_flow_speed"),
                                                           rs.getDouble("winter_free_flow_speed"),
                                                           isSpeedLimitSeason(rs.getInt("speed_limit_season"))));

        return links.stream().collect(Collectors.toMap(LinkFastLaneDto::getNaturalId, Function.identity()));
    }

    private static boolean isSpeedLimitSeason(final int season) {
        return season != SpeedLimitSeason.SUMMER.getCode();
    }
}