package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinateList;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinateListPK;

@Repository
public interface ForecastSectionCoordinateListRepository extends JpaRepository<ForecastSectionCoordinateList, ForecastSectionCoordinateListPK> {

}
