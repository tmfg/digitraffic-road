package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastConditionReason;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeatherPK;

@Repository
public interface ForecastConditionReasonRepository extends JpaRepository<ForecastConditionReason, ForecastSectionWeatherPK> {
}
