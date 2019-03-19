package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinate;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinatePK;

@Repository
public interface ForecastSectionCoordinateRepository extends JpaRepository<ForecastSectionCoordinate, ForecastSectionCoordinatePK> {

}
