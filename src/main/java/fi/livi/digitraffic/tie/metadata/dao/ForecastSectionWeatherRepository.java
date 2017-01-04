package fi.livi.digitraffic.tie.metadata.dao;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeather;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeatherPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastSectionWeatherRepository extends JpaRepository<ForecastSectionWeather, ForecastSectionWeatherPK> {
}
