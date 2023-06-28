package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionWeatherForecastDtoV1;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionWeather;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionWeatherPK;

@Repository
public interface ForecastSectionWeatherRepository extends JpaRepository<ForecastSectionWeather, ForecastSectionWeatherPK> {

    @Query(value =
        "SELECT fs.natural_id AS id\n" +
        "     , fsw.forecast_name AS forecastName\n" +
        "     , fsw.time AS time\n" +
        "     , fsw.daylight AS daylight\n" +
        "     , fsw.overall_road_condition AS overallRoadCondition\n" +
        "     , fsw.reliability AS reliability\n" +
        "     , CAST (fsw.road_temperature AS NUMERIC(10,1)) AS roadTemperature\n" +
        "     , CAST (fsw.temperature AS NUMERIC(10,1)) AS temperature\n" +
        "     , fsw.weather_symbol AS weatherSymbol\n" +
        "     , fsw.wind_direction AS windDirection\n" +
        "     , fsw.wind_speed AS windSpeed\n" +
        "     , fsw.type AS type\n" +
        "     , fcr.precipitation_condition AS precipitationCondition\n" +
        "     , fcr.road_condition AS roadCondition\n" +
        "     , fcr.wind_condition AS windCondition\n" +
        "     , fcr.freezing_rain_condition AS freezingRainCondition\n" +
        "     , fcr.winter_slipperiness AS winterSlipperiness\n" +
        "     , fcr.visibility_condition AS visibilityCondition\n" +
        "     , fcr.friction_condition AS frictionCondition\n" +
        "     , GREATEST(fsw.modified, fcr.modified) AS dataUpdatedTime\n" +
        "FROM FORECAST_SECTION fs\n" +
        "INNER JOIN FORECAST_SECTION_WEATHER fsw ON fsw.forecast_section_id = fs.id\n" +
        "LEFT OUTER JOIN FORECAST_CONDITION_REASON fcr ON fsw.forecast_section_id = fcr.forecast_section_id AND fsw.forecast_name = fcr.forecast_name\n" +
        "WHERE fs.version = :version\n" +
        "  AND (cast(:area AS geometry) IS NULL OR ST_INTERSECTS(cast(:area AS geometry), fs.geometry) = TRUE)\n" +
        "  AND (cast(:id as varchar) IS NULL OR fs.natural_id = :id)\n" +
        "  AND (cast(:roadNumber as integer) IS NULL OR fs.road_number = :roadNumber)\n" +
        "ORDER BY fs.natural_id, fsw.time", nativeQuery = true)
    List<ForecastSectionWeatherForecastDtoV1> findForecastSectionWeatherOrderByIdAndTime(final int version,
                                                                                         final Geometry area,
                                                                                         final Integer roadNumber,
                                                                                         final String id);

    default List<ForecastSectionWeatherForecastDtoV1> findForecastSectionWeatherOrderByIdAndTime(final int version,
                                                                                                 final Geometry area,
                                                                                                 final Integer roadNumber) {
        return findForecastSectionWeatherOrderByIdAndTime(version, area, roadNumber, null);
    }

    default List<ForecastSectionWeatherForecastDtoV1> findForecastSectionWeatherOrderByTime(final int version,
                                                                                            final String id) {
        return findForecastSectionWeatherOrderByIdAndTime(version, null, null, id);
    }

    @Query(value =
        "WITH max_with_filter AS (\n" +
        "    SELECT max(GREATEST(fsw.modified, fcr.modified)) AS modified\n" +
        "    FROM FORECAST_SECTION_WEATHER fsw\n" +
        "    INNER JOIN forecast_section fs ON fsw.forecast_section_id = fs.id\n" +
        "    LEFT OUTER JOIN FORECAST_CONDITION_REASON fcr ON fsw.forecast_section_id = fcr.forecast_section_id AND fsw.forecast_name = fcr.forecast_name\n" +
        "    WHERE fs.version = :version\n" +
        "      AND (cast(:area AS geometry) IS NULL OR ST_INTERSECTS(cast(:area AS geometry), fs.geometry) = TRUE)\n" +
        "      AND (cast(:id as varchar) IS NULL OR fs.natural_id = :id)\n" +
        "      AND (cast(:roadNumber as integer) IS NULL OR fs.road_number = :roadNumber)\n" +
        "), max_from_all AS (\n" +
        "    SELECT max(all_data.modified) AS modified\n" +
        "    FROM (SELECT MAX(fsw.modified) AS modified\n" +
        "          FROM FORECAST_SECTION_WEATHER fsw\n" +
        "          UNION ALL\n" +
        "          SELECT MAX(fcr.modified) AS modified\n" +
        "          FROM FORECAST_CONDITION_REASON fcr) all_data\n" +
        ")\n" +
        "SELECT coalesce(max_with_filter.modified, max_from_all.modified)\n" +
        "FROM max_with_filter, max_from_all", nativeQuery = true)
    Instant getLastModified(final int version,
                            @Nullable final Geometry area,
                            @Nullable final Integer roadNumber,
                            @Nullable final String id);

    default Instant getLastModified(final int version,
                                    final Geometry area,
                                    final Integer roadNumber) {
        return getLastModified(version, area, roadNumber, null);
    }

    default Instant getLastModified(final int version,
                                    final String id) {
        return getLastModified(version, null, null, id);
    }
}
