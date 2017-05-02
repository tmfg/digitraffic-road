package fi.livi.digitraffic.tie.data.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.helper.DaoUtils;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastConditionReason;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeather;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeatherPK;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.FrictionCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.OverallRoadCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.PrecipitationCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.Reliability;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.VisibilityCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.WindCondition;

@Repository
public class ForecastSectionWeatherDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ForecastSectionWeatherDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, List<ForecastSectionWeather>> getForecastSectionWeatherData() {

        final HashMap<String, List<ForecastSectionWeather>> res = new HashMap<>();

        jdbcTemplate.query(
            "SELECT * FROM FORECAST_SECTION_WEATHER fsw " +
            "LEFT OUTER JOIN FORECAST_CONDITION_REASON fcr ON fsw.forecast_section_id = fcr.forecast_section_id AND fsw.forecast_name = fcr.forecast_name " +
            "LEFT OUTER JOIN FORECAST_SECTION fs ON fsw.forecast_section_id = fs.id " +
            "ORDER BY fs.natural_id, fsw.time",
            rs -> {
                String forecastSectionNaturalId = rs.getString("natural_id");

                if (res.containsKey(forecastSectionNaturalId)) {
                    res.get(forecastSectionNaturalId).add(mapForecastSectionWeather(rs));
                } else {
                    final ArrayList<ForecastSectionWeather> list = new ArrayList<>();
                    list.add(mapForecastSectionWeather(rs));
                    res.put(forecastSectionNaturalId, list);
                }
            });

        return res;
    }

    private ForecastSectionWeather mapForecastSectionWeather(final ResultSet rs) throws SQLException {

        final ForecastSectionWeatherPK forecastSectionWeatherPK =
                            new ForecastSectionWeatherPK(rs.getLong("forecast_section_id"), rs.getString("forecast_name"));

        return new ForecastSectionWeather(forecastSectionWeatherPK,
                                          rs.getTimestamp("time"),
                                          DaoUtils.findBoolean(rs, "daylight"),
                                          DaoUtils.findEnum(rs, "overall_road_condition", OverallRoadCondition.class),
                                          DaoUtils.findEnum(rs, "reliability", Reliability.class),
                                          rs.getString("road_temperature"),
                                          rs.getString("temperature"),
                                          rs.getString("weather_symbol"),
                                          DaoUtils.findInteger(rs, "wind_direction"),
                                          DaoUtils.findDouble(rs, "wind_speed"),
                                          rs.getString("type"),
                                          mapForecastConditionReason(rs, forecastSectionWeatherPK));
    }

    private ForecastConditionReason mapForecastConditionReason(final ResultSet rs, final ForecastSectionWeatherPK forecastSectionWeatherPK) throws SQLException {

        final PrecipitationCondition precipitationCondition = DaoUtils.findEnum(rs, "precipitation_condition", PrecipitationCondition.class);
        final RoadCondition roadCondition = DaoUtils.findEnum(rs, "road_condition", RoadCondition.class);
        final WindCondition windCondition = DaoUtils.findEnum(rs, "wind_condition", WindCondition.class);
        final Boolean freezingRainCondition = DaoUtils.findBoolean(rs, "freezing_rain_condition");
        final Boolean winterSlipperiness = DaoUtils.findBoolean(rs, "winter_slipperiness");
        final VisibilityCondition visibilityCondition = DaoUtils.findEnum(rs, "visibility_condition", VisibilityCondition.class);
        final FrictionCondition frictionCondition = DaoUtils.findEnum(rs, "friction_condition", FrictionCondition.class);

        if (ObjectUtils.anyNotNull(precipitationCondition, roadCondition, windCondition, freezingRainCondition, winterSlipperiness,
                                   visibilityCondition, frictionCondition)) {
            return new ForecastConditionReason(forecastSectionWeatherPK, precipitationCondition, roadCondition, windCondition, freezingRainCondition,
                                               winterSlipperiness, visibilityCondition, frictionCondition);
        } else {
            return null;
        }
    }

}
