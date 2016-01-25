package fi.livi.digitraffic.tie.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.LamStationData;

@Repository
public class LamStationRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<LamStationData> listaLamStations() {
        return jdbcTemplate.query(
                "select ls.natural_id as lam_number, rs.name as rws_name, ls.name, 0 as x, 0 as y, 0 as z, rd.name as " +
                        "province " +
                "from lam_station ls, road_district rd, road_station rs " +
                "where ls.road_district_id = rd.id " +
                "and ls.road_station_id = rs.id " +
                "and ls.obsolete = 0", new BeanPropertyRowMapper<LamStationData>(LamStationData.class));
    }
}
