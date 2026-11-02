package fi.livi.digitraffic.tie.dao.weather.forecast;

import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Saves many forecast rows with a few database calls. The normal repository sends one query per row,
 * because the database fills in modified_at.
 */
@Repository
public class ForecastSectionWeatherV2Dao {

    // modified_at is not set here on purpose: a database trigger sets it when a row is updated
    private static final String UPSERT = """
        INSERT INTO forecast_section_weather_v2(forecast_section_id, forecast_name, time, type, daylight, road_temperature, temperature,
                                                wind_speed, wind_direction, overall_road_condition, road_condition, weather_symbol)
        VALUES (:forecastSectionId, :forecastName, :time, :type, :daylight, :roadTemperature, :temperature,
                :windSpeed, :windDirection, :overallRoadCondition, :roadCondition, :weatherSymbol)
        ON CONFLICT (forecast_section_id, forecast_name) DO UPDATE SET
            time = EXCLUDED.time,
            type = EXCLUDED.type,
            daylight = EXCLUDED.daylight,
            road_temperature = EXCLUDED.road_temperature,
            temperature = EXCLUDED.temperature,
            wind_speed = EXCLUDED.wind_speed,
            wind_direction = EXCLUDED.wind_direction,
            overall_road_condition = EXCLUDED.overall_road_condition,
            road_condition = EXCLUDED.road_condition,
            weather_symbol = EXCLUDED.weather_symbol
        """;

    private static final String DELETE =
        "DELETE FROM forecast_section_weather_v2 WHERE forecast_section_id = :forecastSectionId AND forecast_name = :forecastName";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public ForecastSectionWeatherV2Dao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Deletes the given rows and saves the given rows (inserts new, updates existing).
     * The rows already read from the database are dropped from memory afterwards, because they no longer match the database.
     */
    public void deleteAndUpsert(final List<ForecastSectionWeatherV2> toDelete, final List<ForecastSectionWeatherV2> toUpsert) {
        entityManager.flush();
        if (!toDelete.isEmpty()) {
            jdbcTemplate.batchUpdate(DELETE, toDelete.stream().map(ForecastSectionWeatherV2Dao::toDeleteParameters).toArray(MapSqlParameterSource[]::new));
        }
        if (!toUpsert.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT, toUpsert.stream().map(ForecastSectionWeatherV2Dao::toUpsertParameters).toArray(MapSqlParameterSource[]::new));
        }
        entityManager.clear();
    }

    private static MapSqlParameterSource toDeleteParameters(final ForecastSectionWeatherV2 f) {
        return new MapSqlParameterSource()
            .addValue("forecastSectionId", f.getForecastSectionId())
            .addValue("forecastName", f.getForecastName());
    }

    private static MapSqlParameterSource toUpsertParameters(final ForecastSectionWeatherV2 f) {
        // Starts with the same key fields as delete
        return toDeleteParameters(f)
            .addValue("time", f.getTime().atOffset(ZoneOffset.UTC))
            .addValue("type", f.getType().name())
            .addValue("daylight", f.isDaylight())
            .addValue("roadTemperature", f.getRoadTemperature())
            .addValue("temperature", f.getTemperature())
            .addValue("windSpeed", f.getWindSpeed())
            .addValue("windDirection", f.getWindDirection())
            .addValue("overallRoadCondition", f.getOverallRoadCondition() == null ? null : f.getOverallRoadCondition().name())
            .addValue("roadCondition", f.getRoadCondition() == null ? null : f.getRoadCondition().name())
            .addValue("weatherSymbol", f.getWeatherSymbol());
    }
}
