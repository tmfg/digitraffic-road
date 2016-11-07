package fi.livi.digitraffic.tie.metadata.dao;

import fi.livi.digitraffic.tie.metadata.model.ForecastSectionWeather;
import fi.livi.digitraffic.tie.metadata.model.ForecastSectionWeatherPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastSectionWeatherRepository extends JpaRepository<ForecastSectionWeather, ForecastSectionWeatherPK> {
}
