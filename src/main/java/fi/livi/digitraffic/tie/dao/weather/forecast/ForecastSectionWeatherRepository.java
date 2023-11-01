package fi.livi.digitraffic.tie.dao.weather.forecast;

import java.time.Instant;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionWeatherForecastDtoV1;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionWeather;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionWeatherPK;
import jakarta.annotation.Nullable;

@Repository
public interface ForecastSectionWeatherRepository extends JpaRepository<ForecastSectionWeather, ForecastSectionWeatherPK> {

    @Query(value = """            
            SELECT fs.natural_id AS id
                 , fsw.forecast_name AS forecastName
                 , fsw.time AS time
                 , fsw.daylight AS daylight
                 , fsw.overall_road_condition AS overallRoadCondition
                 , fsw.reliability AS reliability
                 , CAST (fsw.road_temperature AS NUMERIC(10,1)) AS roadTemperature
                 , CAST (fsw.temperature AS NUMERIC(10,1)) AS temperature
                 , fsw.weather_symbol AS weatherSymbol
                 , fsw.wind_direction AS windDirection
                 , fsw.wind_speed AS windSpeed
                 , fsw.type AS type
                 , fcr.precipitation_condition AS precipitationCondition
                 , fcr.road_condition AS roadCondition
                 , fcr.wind_condition AS windCondition
                 , fcr.freezing_rain_condition AS freezingRainCondition
                 , fcr.winter_slipperiness AS winterSlipperiness
                 , fcr.visibility_condition AS visibilityCondition
                 , fcr.friction_condition AS frictionCondition
                 , GREATEST(fsw.modified, fcr.modified) AS dataUpdatedTime
            FROM FORECAST_SECTION fs
            INNER JOIN FORECAST_SECTION_WEATHER fsw ON fsw.forecast_section_id = fs.id
            LEFT OUTER JOIN FORECAST_CONDITION_REASON fcr ON fsw.forecast_section_id = fcr.forecast_section_id AND fsw.forecast_name = fcr.forecast_name
            WHERE fs.version = :version
              AND (cast(:area AS geometry) IS NULL OR ST_INTERSECTS(cast(:area AS geometry), fs.geometry) = TRUE)
              AND (cast(:id as varchar) IS NULL OR fs.natural_id = :id)
              AND (cast(:roadNumber as integer) IS NULL OR fs.road_number = :roadNumber)
            ORDER BY fs.natural_id, fsw.time""", nativeQuery = true)
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

    @Query(value = """
            WITH max_with_filter AS (
                SELECT max(GREATEST(fsw.modified, fcr.modified)) AS modified
                FROM FORECAST_SECTION_WEATHER fsw
                INNER JOIN forecast_section fs ON fsw.forecast_section_id = fs.id
                LEFT OUTER JOIN FORECAST_CONDITION_REASON fcr ON fsw.forecast_section_id = fcr.forecast_section_id AND fsw.forecast_name = fcr.forecast_name
                WHERE fs.version = :version
                  AND (cast(:area AS geometry) IS NULL OR ST_INTERSECTS(cast(:area AS geometry), fs.geometry) = TRUE)
                  AND (cast(:id as varchar) IS NULL OR fs.natural_id = :id)
                  AND (cast(:roadNumber as integer) IS NULL OR fs.road_number = :roadNumber)
            ), max_from_all AS (
                SELECT max(all_data.modified) AS modified
                FROM (SELECT MAX(fsw.modified) AS modified
                      FROM FORECAST_SECTION_WEATHER fsw
                      UNION ALL
                      SELECT MAX(fcr.modified) AS modified
                      FROM FORECAST_CONDITION_REASON fcr) all_data
            )
            SELECT coalesce(max_with_filter.modified, max_from_all.modified)
            FROM max_with_filter, max_from_all""", nativeQuery = true)
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
