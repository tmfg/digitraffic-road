package fi.livi.digitraffic.tie.dao.weather.forecast;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherForecastDtoV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2PK;

@Repository
public interface ForecastSectionWeatherV2Repository extends JpaRepository<ForecastSectionWeatherV2, ForecastSectionWeatherV2PK> {

    @Query("SELECT w FROM ForecastSectionWeatherV2 w WHERE w.id.forecastSectionId IN (:forecastSectionIds)")
    List<ForecastSectionWeatherV2> findByForecastSectionIds(final List<Long> forecastSectionIds);

    @Modifying
    @Query("DELETE FROM ForecastSectionWeatherV2 w WHERE w.id.forecastSectionId = :forecastSectionId AND w.id.forecastName NOT IN (:forecastNames)")
    int deleteByForecastSectionIdAndForecastNameNotIn(final long forecastSectionId, final List<String> forecastNames);

    /** Forecasts of detailed forecast sections (forecast_section.type = DETAILED) */
    @Query(value = """
            SELECT fs.natural_id AS id
                 , fsw.forecast_name AS forecastName
                 , fsw.time AS time
                 , fsw.type AS type
                 , fsw.daylight AS daylight
                 , CAST (fsw.road_temperature AS NUMERIC(10,1)) AS roadTemperature
                 , CAST (fsw.temperature AS NUMERIC(10,1)) AS temperature
                 , CAST (fsw.wind_speed AS NUMERIC(10,1)) AS windSpeed
                 , fsw.wind_direction AS windDirection
                 , fsw.overall_road_condition AS overallRoadCondition
                 , fsw.road_condition AS roadCondition
                 , fsw.weather_symbol AS weatherSymbol
            FROM forecast_section fs
            INNER JOIN forecast_section_weather_v2 fsw ON fsw.forecast_section_id = fs.id
            WHERE fs.type = :#{T(fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionType).DETAILED.name()}
              AND fs.obsolete_date IS NULL
              AND (cast(:area AS geometry) IS NULL OR ST_INTERSECTS(cast(:area AS geometry), fs.geometry) = TRUE)
              AND (cast(:id as varchar) IS NULL OR fs.natural_id = :id)
              AND (cast(:roadNumber as integer) IS NULL OR fs.road_number = :roadNumber)
            ORDER BY fs.natural_id, fsw.time""", nativeQuery = true)
    List<ForecastSectionWeatherForecastDtoV2> findDetailedForecasts(final Geometry area, final Integer roadNumber, final String id);
}
